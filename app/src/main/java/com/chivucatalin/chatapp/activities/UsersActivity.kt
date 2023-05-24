package com.chivucatalin.chatapp.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.chivucatalin.chatapp.adapters.UsersAdapter
import com.chivucatalin.chatapp.databinding.ActivityUsersBinding
import com.chivucatalin.chatapp.listeners.UserListener
import com.chivucatalin.chatapp.models.User
import com.chivucatalin.chatapp.utilities.Constants
import com.chivucatalin.chatapp.utilities.PreferenceManager
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot

class UsersActivity : BaseActivity(), UserListener {
    private var binding: ActivityUsersBinding? = null
    private var preferenceManager: PreferenceManager? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUsersBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        preferenceManager = PreferenceManager(applicationContext)
        setListeners()
        users
    }

    private fun setListeners() {
        binding!!.imageBack.setOnClickListener { v: View? -> onBackPressed() }
    }

    private val users: Unit
        private get() {
            loading(true)
            val database = FirebaseFirestore.getInstance()
            database.collection(Constants.KEY_COLLECTION_USERS)
                .get()
                .addOnCompleteListener { task: Task<QuerySnapshot?> ->
                    loading(false)
                    val currentUserId = preferenceManager!!.getString(Constants.KEY_USER_ID)
                    if (task.isSuccessful && task.result != null) {
                        val users: MutableList<User> = ArrayList()
                        for (queryDocumentSnapshot in task.result!!) {
                            if (currentUserId == queryDocumentSnapshot.id) {
                                continue
                            }
                            val user = User()
                            user.name = queryDocumentSnapshot.getString(Constants.KEY_NAME)
                            user.email = queryDocumentSnapshot.getString(Constants.KEY_EMAIL)
                            user.image = queryDocumentSnapshot.getString(Constants.KEY_IMAGE)
                            user.token = queryDocumentSnapshot.getString(Constants.KEY_FCM_TOKEN)
                            user.id = queryDocumentSnapshot.id
                            users.add(user)
                        }
                        if (users.size > 0) {
                            val usersAdapter = UsersAdapter(users, this)
                            binding!!.userRecyclerView.adapter = usersAdapter
                            binding!!.userRecyclerView.visibility = View.VISIBLE
                        } else {
                            showErrorMessage()
                        }
                    } else {
                        showErrorMessage()
                    }
                }
        }

    private fun showErrorMessage() {
        binding!!.textErrorMessage.text = String.format("%s", "No user available")
        binding!!.textErrorMessage.visibility = View.VISIBLE
    }

    private fun loading(isLoading: Boolean) {
        if (isLoading) {
            binding!!.progressBar.visibility = View.VISIBLE
        } else {
            binding!!.progressBar.visibility = View.INVISIBLE
        }
    }

    override fun onUserClicked(user: User?) {
        val intent = Intent(applicationContext, ChatActivity::class.java)
        intent.putExtra(Constants.KEY_USER, user)
        startActivity(intent)
        finish()
    }

}