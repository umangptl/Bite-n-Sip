package com.cmpe277.bitensip

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.cmpe277.bitensip.databinding.ActivityGptBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

class GPTActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGptBinding
    private lateinit var adapter: ChatAdapter
    private val chatMessages = mutableListOf<ChatMessage>()
    private val OPENAI_API_KEY = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGptBinding.inflate(layoutInflater)
        setContentView(binding.root)

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

        adapter = ChatAdapter(chatMessages)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        binding.sendButton.setOnClickListener {
            val userInput = binding.editText.text.toString().trim()
            if (userInput.isNotEmpty()) {
                addMessage(userInput, isUser = true)
                binding.editText.text.clear()
                sendToLLM(userInput)
            }
        }
    }

    private fun addMessage(message: String, isUser: Boolean) {
        chatMessages.add(ChatMessage(message, isUser))
        adapter.notifyItemInserted(chatMessages.size - 1)
        binding.recyclerView.scrollToPosition(chatMessages.size - 1)
    }

    private fun sendToLLM(prompt: String) {
        thread {
            try {
                val url = URL("https://models.inference.ai.azure.com/chat/completions")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.setRequestProperty("Authorization", "Bearer $OPENAI_API_KEY")
                connection.doOutput = true

                val jsonInputString = createOpenAIJsonInput(prompt)

                val writer = OutputStreamWriter(connection.outputStream)
                writer.write(jsonInputString)
                writer.flush()

                val response = BufferedReader(InputStreamReader(connection.inputStream)).use { it.readText() }
                val gptResponse = parseOpenAIResponse(response)

                runOnUiThread {
                    addMessage(gptResponse, isUser = false)
                }

            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    addMessage("Error: Unable to fetch response", isUser = false)
                }
            }
        }
    }

    private fun createOpenAIJsonInput(prompt: String): String {
        return try {
            val json = JSONObject()
            val messages = JSONArray()

            val systemMessage = JSONObject()
            systemMessage.put("role", "system")
            systemMessage.put("content", "You are a helpful fitness and nutrition assistant.")
            messages.put(systemMessage)

            val userMessage = JSONObject()
            userMessage.put("role", "user")
            userMessage.put("content", prompt)
            messages.put(userMessage)

            json.put("messages", messages)
            json.put("temperature", 1.0)
            json.put("top_p", 1.0)
            json.put("max_tokens", 1000)
            json.put("model", "gpt-4o-mini")

            json.toString()
        } catch (e: Exception) {
            ""
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