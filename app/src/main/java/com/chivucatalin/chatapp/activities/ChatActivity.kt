package com.chivucatalin.chatapp.activities

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.View
import com.chivucatalin.chatapp.adapters.ChatAdapter
import com.chivucatalin.chatapp.databinding.ActivityChatBinding
import com.chivucatalin.chatapp.models.ChatMessage
import com.chivucatalin.chatapp.models.User
import com.chivucatalin.chatapp.utilities.Constants
import com.chivucatalin.chatapp.utilities.PreferenceManager
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.*
import com.google.firebase.firestore.EventListener
import java.text.SimpleDateFormat
import java.util.*


class ChatActivity : BaseActivity() {
    private var binding: ActivityChatBinding? = null
    private var receiverUser: User? = null
    private var chatMessages: MutableList<ChatMessage>? = null
    private var chatAdapter: ChatAdapter? = null
    private var preferenceManager: PreferenceManager? = null
    private var database: FirebaseFirestore? = null
    private var conversionId: String? = null
    private var isReceiverAvailable = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        setListeners()
        loadReceiverDetails()
        init()
        listenMessages()
    }

    private fun init() {
        preferenceManager = PreferenceManager(applicationContext)
        chatMessages = ArrayList()
        chatAdapter = ChatAdapter(
            chatMessages as ArrayList<ChatMessage>,
            getBitmapFromEncodedString(receiverUser!!.image),
            preferenceManager!!.getString(Constants.KEY_USER_ID)
        )
        binding!!.chatRecyclerView.adapter = chatAdapter
        database = FirebaseFirestore.getInstance()
    }

    private fun sendMessage() {
        val message = HashMap<String, Any>()
        message[Constants.KEY_SENDER_ID] =
            this.preferenceManager!!.getString(Constants.KEY_USER_ID)  ?: "default value"
        message[Constants.KEY_RECEIVER_ID] = this.receiverUser!!.id  ?: "default value"
        message[Constants.KEY_MESSAGE] =
            binding!!.inputMessage.text.toString()
        message[Constants.KEY_TIMESTAMP] = Date()
        database!!.collection(Constants.KEY_COLLECTION_CHAT).add(message)
        if (conversionId != null) {
            updateConversion(binding!!.inputMessage.text.toString())
        } else {
            val conversion = HashMap<String, Any>()
            conversion[Constants.KEY_SENDER_ID] =
                this.preferenceManager!!.getString(Constants.KEY_USER_ID)  ?: "default value"
            conversion[Constants.KEY_SENDER_NAME] =
                preferenceManager!!.getString(Constants.KEY_NAME)  ?: "default value"
            conversion[Constants.KEY_SENDER_IMAGE] =
                preferenceManager!!.getString(Constants.KEY_IMAGE)  ?: "default value"
            conversion[Constants.KEY_RECEIVER_ID] = this.receiverUser!!.id  ?: "default value"
            conversion[Constants.KEY_RECEIVER_NAME] = this.receiverUser!!.name  ?: "default value"
            conversion[Constants.KEY_RECEIVER_IMAGE] = this.receiverUser!!.image  ?: "default value"
            conversion[Constants.KEY_LAST_MESSAGE] =
                binding!!.inputMessage.text.toString()
            conversion[Constants.KEY_TIMESTAMP] = Date()
            addConversion(conversion)
        }
        binding!!.inputMessage.text = null
    }

    private fun listenAvailabilityOfReceiver() {
        database!!.collection(Constants.KEY_COLLECTION_USERS).document(
            receiverUser!!.id.toString()
        )
            .addSnapshotListener(this@ChatActivity) { value: DocumentSnapshot?, error: FirebaseFirestoreException? ->
                if (error != null) {
                    return@addSnapshotListener
                }
                if (value != null) {
                    if (value.getLong(Constants.KEY_AVAILABILITY) != null) {
                        val availability = Objects.requireNonNull(
                            value.getLong(Constants.KEY_AVAILABILITY)
                        )?.toInt()
                        isReceiverAvailable = availability == 1
                    }
                }
                if (isReceiverAvailable) {
                    binding!!.textAvailability.visibility = View.VISIBLE
                } else {
                    binding!!.textAvailability.visibility = View.GONE
                }
            }
    }

    private fun listenMessages() {
        database!!.collection(Constants.KEY_COLLECTION_CHAT)
            .whereEqualTo(
                Constants.KEY_SENDER_ID,
                preferenceManager!!.getString(Constants.KEY_USER_ID)
            )
            .whereEqualTo(Constants.KEY_RECEIVER_ID, receiverUser!!.id)
            .addSnapshotListener(eventListener)
        database!!.collection(Constants.KEY_COLLECTION_CHAT)
            .whereEqualTo(Constants.KEY_SENDER_ID, receiverUser!!.id)
            .whereEqualTo(
                Constants.KEY_RECEIVER_ID,
                preferenceManager!!.getString(Constants.KEY_USER_ID)
            )
            .addSnapshotListener(eventListener)
    }

    private val eventListener =
        EventListener { value: QuerySnapshot?, error: FirebaseFirestoreException? ->
            if (error != null) {
                return@EventListener
            }
            if (value != null) {
                val count = chatMessages!!.size
                for (documentChange in value.documentChanges) {
                    if (documentChange.type == DocumentChange.Type.ADDED) {
                        val chatMessage = ChatMessage()
                        chatMessage.senderId =
                            documentChange.document.getString(Constants.KEY_SENDER_ID)
                        chatMessage.receiverId =
                            documentChange.document.getString(Constants.KEY_RECEIVER_ID)
                        chatMessage.message =
                            documentChange.document.getString(Constants.KEY_MESSAGE)
                        chatMessage.dateTime =
                            getReadableDateTime(documentChange.document.getDate(Constants.KEY_TIMESTAMP))
                        chatMessage.dateObject =
                            documentChange.document.getDate(Constants.KEY_TIMESTAMP)
                        chatMessages!!.add(chatMessage)
                    }
                }
                Collections.sort(chatMessages) { obj1: ChatMessage, obj2: ChatMessage ->
                    obj1.dateObject!!.compareTo(
                        obj2.dateObject
                    )
                }
                if (count == 0) {
                    chatAdapter!!.notifyDataSetChanged()
                } else {
                    chatAdapter!!.notifyItemRangeInserted(chatMessages!!.size, chatMessages!!.size)
                    binding!!.chatRecyclerView.smoothScrollToPosition(chatMessages!!.size - 1)
                }
                binding!!.chatRecyclerView.visibility = View.VISIBLE
            }
            binding!!.progressBar.visibility = View.GONE
            if (conversionId == null) {
                checkForConversion()
            }
        }

    private fun getBitmapFromEncodedString(encodedImage: String?): Bitmap {
        val bytes = Base64.decode(encodedImage, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    private fun loadReceiverDetails() {
        receiverUser = intent.getSerializableExtra(Constants.KEY_USER) as User?
        binding!!.textName.text = receiverUser!!.name
    }

    private fun setListeners() {
        binding!!.imageBack.setOnClickListener { v: View? -> onBackPressed() }
        binding!!.layoutSend.setOnClickListener { v: View? -> sendMessage() }
    }

    private fun getReadableDateTime(date: Date?): String {
        return SimpleDateFormat("MMMM dd, yyyy - hh:mm a", Locale.getDefault()).format(date)
    }

    private fun addConversion(conversion: HashMap<String, Any>) {
        database!!.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
            .add(conversion)
            .addOnSuccessListener { documentReference: DocumentReference ->
                conversionId = documentReference.id
            }
    }

    private fun updateConversion(message: String) {
        val documentReference =
            database!!.collection(Constants.KEY_COLLECTION_CONVERSATIONS).document(
                conversionId!!
            )
        documentReference.update(
            Constants.KEY_LAST_MESSAGE, message,
            Constants.KEY_TIMESTAMP, Date()
        )
    }

    private fun checkForConversion() {
        if (chatMessages!!.size != 0) {
            checkForConversionRemotely(
                preferenceManager!!.getString(Constants.KEY_USER_ID),
                receiverUser!!.id
            )
            checkForConversionRemotely(
                receiverUser!!.id,
                preferenceManager!!.getString(Constants.KEY_USER_ID)
            )
        }
    }

    private fun checkForConversionRemotely(senderId: String?, receiverId: String?) {
        database!!.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
            .whereEqualTo(Constants.KEY_SENDER_ID, senderId)
            .whereEqualTo(Constants.KEY_RECEIVER_ID, receiverId)
            .get()
            .addOnCompleteListener(conversionOnCompleteListener)
    }

    private val conversionOnCompleteListener = OnCompleteListener { task: Task<QuerySnapshot?> ->
        if (task.isSuccessful && task.result != null && task.result!!
                .documents.size > 0
        ) {
            val documentSnapshot = task.result!!.documents[0]
            conversionId = documentSnapshot.id
        }
    }

    override fun onResume() {
        super.onResume()
        listenAvailabilityOfReceiver()
    }
}