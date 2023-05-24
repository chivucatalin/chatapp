package com.chivucatalin.chatapp.listeners

import com.chivucatalin.chatapp.models.User

interface UserListener {
    fun onUserClicked(user: User?)
}