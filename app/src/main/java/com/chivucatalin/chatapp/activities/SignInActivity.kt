package com.chivucatalin.chatapp.activities

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.chivucatalin.chatapp.databinding.ActivitySignInBinding
import com.chivucatalin.chatapp.utilities.Constants
import com.chivucatalin.chatapp.utilities.PreferenceManager
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot

class SignInActivity : AppCompatActivity() {
    private var binding: ActivitySignInBinding? = null
    private var preferenceManager: PreferenceManager? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferenceManager = PreferenceManager(applicationContext)
        if (preferenceManager!!.getBoolean(Constants.KEY_IS_SIGNED_IN)) {
            val intent = Intent(applicationContext, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        setListeners()
    }

    private fun setListeners() {
        binding!!.textCreateNewAccount.setOnClickListener { v: View? ->
            startActivity(
                Intent(
                    applicationContext, SignUpActivity::class.java
                )
            )
        }
        binding!!.buttonSignIn.setOnClickListener { v: View? ->
            if (isValidSignInDetails) {
                signIn()
            }
        }
    }

    private fun signIn() {
        loading(true)
        val database = FirebaseFirestore.getInstance()
        database.collection(Constants.KEY_COLLECTION_USERS)
            .whereEqualTo(Constants.KEY_EMAIL, binding!!.inputEmail.text.toString())
            .whereEqualTo(Constants.KEY_PASSWORD, binding!!.inputPassword.text.toString())
            .get()
            .addOnCompleteListener { task: Task<QuerySnapshot?> ->
                if (task.isSuccessful && task.result != null && task.result!!
                        .documents.size > 0
                ) {
                    val documentSnapshot = task.result!!.documents[0]
                    preferenceManager!!.putBoolean(Constants.KEY_IS_SIGNED_IN, true)
                    preferenceManager!!.putString(Constants.KEY_USER_ID, documentSnapshot.id)
                    preferenceManager!!.putString(
                        Constants.KEY_NAME,
                        documentSnapshot.getString(Constants.KEY_NAME)
                    )
                    preferenceManager!!.putString(
                        Constants.KEY_IMAGE,
                        documentSnapshot.getString(Constants.KEY_IMAGE)
                    )
                    val intent = Intent(applicationContext, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(intent)
                } else {
                    loading(false)
                    showToast("Unable to sign in")
                }
            }
    }

    private fun loading(isLoading: Boolean) {
        if (isLoading) {
            binding!!.buttonSignIn.visibility = View.INVISIBLE
            binding!!.progressBar.visibility = View.VISIBLE
        } else {
            binding!!.progressBar.visibility = View.INVISIBLE
            binding!!.buttonSignIn.visibility = View.VISIBLE
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
    }

    private val isValidSignInDetails: Boolean
        private get() = if (binding!!.inputEmail.text.toString().trim { it <= ' ' }.isEmpty()) {
            showToast("Enter email")
            false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(binding!!.inputEmail.text.toString())
                .matches()
        ) {
            showToast("Enter valid image")
            false
        } else if (binding!!.inputPassword.text.toString().trim { it <= ' ' }.isEmpty()) {
            showToast("Enter password")
            false
        } else {
            true
        }
}