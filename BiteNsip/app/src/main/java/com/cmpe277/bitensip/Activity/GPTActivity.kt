package com.cmpe277.bitensip

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.cmpe277.bitensip.databinding.ActivityGptBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.Base64
import kotlin.concurrent.thread

class GPTActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGptBinding
    private lateinit var adapter: ChatAdapter
    private val chatMessages = mutableListOf<ChatMessage>()
    private var selectedImageUri: Uri? = null
    private val OPENAI_API_KEY = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGptBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupBottomNavigation()
        setupRecyclerView()
        setupButtons()
    }

    private fun setupBottomNavigation() {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView?.selectedItemId = R.id.nav_gpt
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    true
                }
                R.id.nav_food -> {
                    startActivity(Intent(this, FoodActivity::class.java))
                    true
                }
                R.id.nav_water -> {
                    startActivity(Intent(this, WaterActivity::class.java))
                    true
                }
                R.id.nav_gpt -> {
                    startActivity(Intent(this, GPTActivity::class.java))
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = ChatAdapter(chatMessages)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        binding.sendButton.setOnClickListener {
            val userInput = binding.editText.text.toString().trim()
            if (userInput.isNotEmpty() || selectedImageUri != null) {
                addMessage(userInput, selectedImageUri, isUser = true)
                binding.editText.text.clear()
                sendToLLM(userInput, selectedImageUri)
                selectedImageUri = null // Reset after sending.
                binding.imagePreviewContainer.visibility = View.GONE // Hide preview after sending.
            }
        }
    }

    private fun setupButtons() {
        // Image picker logic
        val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                selectedImageUri = it // Save the selected image URI for later use in API call.
                binding.previewImageView.setImageURI(it)
                binding.imagePreviewContainer.visibility = View.VISIBLE
            }
        }

        binding.uploadButton.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.discardImageIcon.setOnClickListener {
            selectedImageUri = null
            binding.imagePreviewContainer.visibility = View.GONE
        }
    }

    private fun addMessage(textMessage: String, imageUri: Uri?, isUser: Boolean) {
        chatMessages.add(ChatMessage(textMessage, imageUri, isUser))
        adapter.notifyItemInserted(chatMessages.size - 1)
        binding.recyclerView.scrollToPosition(chatMessages.size - 1)
    }

    private fun sendToLLM(prompt: String, imageUri: Uri?) {
        thread {
            try {
                val url = URL("https://models.inference.ai.azure.com/chat/completions")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.setRequestProperty("Authorization", "Bearer $OPENAI_API_KEY")
                connection.doOutput = true

                val jsonInputString = createOpenAIJsonInput(prompt, imageUri)

                val writer = OutputStreamWriter(connection.outputStream)
                writer.write(jsonInputString)
                writer.flush()

                val response = BufferedReader(InputStreamReader(connection.inputStream)).use { it.readText() }
                val gptResponse = parseOpenAIResponse(response)

                runOnUiThread {
                    addMessage(gptResponse, null, false) // Assuming response is text only.
                }

            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    addMessage("Error: Unable to fetch response", null, false)
                    selectedImageUri = null // Reset on error.
                }
            }
        }
    }

    private fun createOpenAIJsonInput(prompt: String, imageUri: Uri?): String {
        return try {
            val json = JSONObject()
            val messages = JSONArray()

            val systemMessage = JSONObject()
            systemMessage.put("role", "system")
            systemMessage.put("content", "You are a fitness and nutrition assistant.")
            messages.put(systemMessage)

            if (imageUri != null) {
                val imageBase64 = encodeImageToBase64(imageUri)

                val userMessageWithImage = JSONObject()
                userMessageWithImage.put("role", "user")

                // Create a JSON array for content to include both text and image.
                val contentArray = JSONArray()

                // Add text prompt as part of the content.
                if (prompt.isNotEmpty()) {
                    contentArray.put(JSONObject().put("text", prompt).put("type", "text"))
                }

                // Add image data as part of the content.
                contentArray.put(JSONObject().put("image_url", JSONObject().put("url", "data:image/jpeg;base64,$imageBase64").put("detail", "low")).put("type", "image_url"))

                userMessageWithImage.put("content", contentArray)

                messages.put(userMessageWithImage)
            } else if (prompt.isNotEmpty()) {
                // If there's no image, just send the text prompt.
                val userMessageOnlyText = JSONObject()
                userMessageOnlyText.put("role", "user")
                userMessageOnlyText.put("content", prompt)
                messages.put(userMessageOnlyText)
            }

            json.put("messages", messages)
            json.put("temperature", 1.0)
            json.put("top_p", 1.0)
            json.put("max_tokens", 1000)
            json.put("model", "gpt-4o")

            json.toString()
        } catch (e: Exception) {
            ""
        }
    }

    private fun encodeImageToBase64(uri: Uri): String? {
        return try {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                ByteArrayOutputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                    Base64.getEncoder().encodeToString(outputStream.toByteArray())
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun parseOpenAIResponse(jsonResponse: String): String {
        return try {
            val json = JSONObject(jsonResponse)
            val choices = json.getJSONArray("choices")
            val firstChoice = choices.getJSONObject(0)
            val message = firstChoice.getJSONObject("message")

            message.getString("content")
        } catch (e: Exception) {
            "Error parsing response: ${e.message}"
        }
    }
}