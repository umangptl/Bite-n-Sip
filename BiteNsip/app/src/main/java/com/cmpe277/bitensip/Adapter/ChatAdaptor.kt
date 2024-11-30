package com.cmpe277.bitensip

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cmpe277.bitensip.databinding.ItemChatMessageBinding

data class ChatMessage(
    val text: String,
    val imageUri: Uri? = null,
    val isUser: Boolean
)
class ChatAdapter(private val messages: List<ChatMessage>) :
    RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val binding = ItemChatMessageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ChatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chatMessage = messages[position]
        holder.bind(chatMessage)
    }

    override fun getItemCount(): Int = messages.size

    inner class ChatViewHolder(private val binding: ItemChatMessageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(chatMessage: ChatMessage) {
            if (chatMessage.isUser) {
                binding.userMessage.text = chatMessage.text
                binding.userMessage.visibility = View.VISIBLE
                binding.gptMessage.visibility = View.GONE
                binding.userImage.visibility = if (chatMessage.imageUri != null) View.VISIBLE else View.GONE
                chatMessage.imageUri?.let { binding.userImage.setImageURI(it) }
            } else {
                binding.gptMessage.text = chatMessage.text
                binding.gptMessage.visibility = View.VISIBLE
                binding.userMessage.visibility = View.GONE
                binding.gptImage.visibility = if (chatMessage.imageUri != null) View.VISIBLE else View.GONE
                chatMessage.imageUri?.let { binding.gptImage.setImageURI(it) }
            }
        }
    }
}