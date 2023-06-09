package com.chivucatalin.chatapp.adapters

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.chivucatalin.chatapp.databinding.ItemContainerReceivedMessageBinding
import com.chivucatalin.chatapp.databinding.ItemContainerSentMessageBinding
import com.chivucatalin.chatapp.models.ChatMessage

class ChatAdapter(
    private val chatMessages: List<ChatMessage>,
    private val receiverProfileImage: Bitmap,
    private val senderId: String?
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_SENT) {
            SentMessageViewHolder(
                ItemContainerSentMessageBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        } else {
            ReceivedMessageViewHolder(
                ItemContainerReceivedMessageBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == VIEW_TYPE_SENT) {
            (holder as SentMessageViewHolder).setData(chatMessages[position])
        } else {
            (holder as ReceivedMessageViewHolder).setData(
                chatMessages[position],
                receiverProfileImage
            )
        }
    }

    override fun getItemCount(): Int {
        return chatMessages.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (chatMessages[position].senderId == senderId) {
            VIEW_TYPE_SENT
        } else {
            VIEW_TYPE_RECEIVED
        }
    }

    internal class SentMessageViewHolder(private val binding: ItemContainerSentMessageBinding) :
        RecyclerView.ViewHolder(
            binding.root
        ) {
        fun setData(chatMessage: ChatMessage) {
            binding.textMessage.text = chatMessage.message
            binding.textDateTime.text = chatMessage.dateTime
        }
    }

    internal class ReceivedMessageViewHolder(private val binding: ItemContainerReceivedMessageBinding) :
        RecyclerView.ViewHolder(
            binding.root
        ) {
        fun setData(chatMessage: ChatMessage, receiverProfileImage: Bitmap?) {
            binding.textMessage.text = chatMessage.message
            binding.textDateTime.text = chatMessage.dateTime
            binding.imageProfile.setImageBitmap(receiverProfileImage)
        }
    }

    companion object {
        const val VIEW_TYPE_SENT = 1
        const val VIEW_TYPE_RECEIVED = 2
    }
}