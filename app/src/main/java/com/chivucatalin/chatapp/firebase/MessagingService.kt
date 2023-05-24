package com.chivucatalin.chatapp.firebase

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        //        Log.d("FCM", "Token: " + token);
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        //        Log.d("FCM", "Message: " + remoteMessage.getNotification().getBody());
    }
}