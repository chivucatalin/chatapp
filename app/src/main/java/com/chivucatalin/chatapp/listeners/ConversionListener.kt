package com.chivucatalin.chatapp.listeners

import com.chivucatalin.chatapp.models.User

interface ConversionListener {
    fun onConversionClick(user: User?)
}