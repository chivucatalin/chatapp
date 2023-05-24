package com.chivucatalin.chatapp.adapters

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.chivucatalin.chatapp.adapters.RecentConversationsAdapter.ConversionViewHolder
import com.chivucatalin.chatapp.databinding.ItemContainerRecentConversionBinding
import com.chivucatalin.chatapp.listeners.ConversionListener
import com.chivucatalin.chatapp.models.ChatMessage
import com.chivucatalin.chatapp.models.User

class RecentConversationsAdapter(
    private val chatMessages: List<ChatMessage>,
    private val conversionListener: ConversionListener
) : RecyclerView.Adapter<ConversionViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversionViewHolder {
        return ConversionViewHolder(
            ItemContainerRecentConversionBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ConversionViewHolder, position: Int) {
        holder.setData(chatMessages[position])
    }

    override fun getItemCount(): Int {
        return chatMessages.size
    }

    inner class ConversionViewHolder(var binding: ItemContainerRecentConversionBinding) :
        RecyclerView.ViewHolder(
            binding.root
        ) {
        fun setData(chatMessage: ChatMessage) {
            binding.imageProfile.setImageBitmap(getConversionImage(chatMessage.conversionImage))
            binding.textName.text = chatMessage.conversionName
            binding.textRecentMessage.text = chatMessage.message
            binding.root.setOnClickListener { v: View? ->
                val user = User()
                user.id = chatMessage.conversionId
                user.name = chatMessage.conversionName
                user.image = chatMessage.conversionImage
                conversionListener.onConversionClick(user)
            }
        }
    }

    private fun getConversionImage(encodedImage: String?): Bitmap {
        val bytes = Base64.decode(encodedImage, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }
}