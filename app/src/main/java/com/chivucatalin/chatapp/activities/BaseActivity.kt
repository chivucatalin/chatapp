package com.chivucatalin.chatapp.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.chivucatalin.chatapp.utilities.Constants
import com.chivucatalin.chatapp.utilities.PreferenceManager
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore

open class BaseActivity : AppCompatActivity() {
    private var documentReference: DocumentReference? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val preferenceManager = PreferenceManager(
            applicationContext
        )
        val database = FirebaseFirestore.getInstance()
        documentReference = preferenceManager.getString(Constants.KEY_USER_ID)?.let {
            database.collection(Constants.KEY_COLLECTION_USERS)
                .document(it)
        }
    }

    override fun onPause() {
        super.onPause()
        documentReference!!.update(Constants.KEY_AVAILABILITY, 0)
    }

    override fun onResume() {
        super.onResume()
        documentReference!!.update(Constants.KEY_AVAILABILITY, 1)
    }
}