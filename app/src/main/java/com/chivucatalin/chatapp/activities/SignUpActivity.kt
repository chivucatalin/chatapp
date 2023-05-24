package com.chivucatalin.chatapp.activities

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.chivucatalin.chatapp.databinding.ActivitySignUpBinding
import com.chivucatalin.chatapp.utilities.Constants
import com.chivucatalin.chatapp.utilities.PreferenceManager
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException

class SignUpActivity : AppCompatActivity() {
    private var binding: ActivitySignUpBinding? = null
    private var preferenceManager: PreferenceManager? = null
    private var encodedImage: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        preferenceManager = PreferenceManager(applicationContext)
        setListeners()
    }

    private fun setListeners() {
        binding!!.textSignIn.setOnClickListener { v: View? -> onBackPressed() }
        binding!!.buttonSignUp.setOnClickListener { v: View? ->
            if (isValidSignUpDetails) {
                signUp()
            }
        }
        binding!!.layoutImage.setOnClickListener { v: View? ->
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            selectImageOrTakePhoto()
        }
    }

    private fun showToast(message: String?) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
    }

    private fun signUp() {
        loading(true)
        val database = FirebaseFirestore.getInstance()
        val user = HashMap<String, Any?>()
        user[Constants.KEY_NAME] = binding!!.inputName.text.toString()
        user[Constants.KEY_EMAIL] = binding!!.inputEmail.text.toString()
        user[Constants.KEY_PASSWORD] = binding!!.inputPassword.text.toString()
        user[Constants.KEY_IMAGE] = encodedImage
        database.collection(Constants.KEY_COLLECTION_USERS)
            .add(user)
            .addOnSuccessListener { documentReference: DocumentReference ->
                loading(false)
                preferenceManager!!.putBoolean(Constants.KEY_IS_SIGNED_IN, true)
                preferenceManager!!.putString(Constants.KEY_USER_ID, documentReference.id)
                preferenceManager!!.putString(
                    Constants.KEY_NAME,
                    binding!!.inputName.text.toString()
                )
                preferenceManager!!.putString(Constants.KEY_IMAGE, encodedImage)
                val intent = Intent(applicationContext, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
            }
            .addOnFailureListener { exception: Exception ->
                loading(false)
                showToast(exception.message)
            }
    }

    private fun encodeImage(bitmap: Bitmap): String {
        val previewWidth = 150
        val previewHeight = bitmap.height * previewWidth / bitmap.width
        val previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false)
        val byteArrayOutputStream = ByteArrayOutputStream()
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream)
        val bytes = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(bytes, Base64.DEFAULT)
    }

    private val pickImage = registerForActivityResult<Intent, ActivityResult>(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == RESULT_OK) {
            if (result.data != null) {
                val imageUri = result.data!!.data
                try {
                    val inputStream = contentResolver.openInputStream(imageUri!!)
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    binding!!.imageProfile.setImageBitmap(bitmap)
                    binding!!.textAddImage.visibility = View.GONE
                    encodedImage = encodeImage(bitmap)
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                }
            }
        }
    }

    private val takePhoto = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { result ->
        if (result != null) {
            binding?.imageProfile?.setImageBitmap(result)
            binding?.textAddImage?.visibility = View.GONE
            encodedImage = encodeImage(result)
        }
    }


    fun selectImageOrTakePhoto() {
        val options = arrayOf<CharSequence>("Take Photo", "Choose from Gallery", "Cancel")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Add Photo!")
        builder.setItems(options) { dialog, item ->
            when {
                options[item] == "Take Photo" -> {
                    takePhoto.launch(null)
                }
                options[item] == "Choose from Gallery" -> {
                    val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    pickImage.launch(galleryIntent)
                }
                options[item] == "Cancel" -> {
                    dialog.dismiss()
                }
            }
        }
        builder.show()
    }
    private val isValidSignUpDetails: Boolean
        private get() = if (encodedImage == null) {
            showToast("Select profile image")
            false
        } else if (binding!!.inputName.text.toString().trim { it <= ' ' }.isEmpty()) {
            showToast("Enter name")
            false
        } else if (binding!!.inputEmail.text.toString().trim { it <= ' ' }.isEmpty()) {
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
        } else if (binding!!.inputConfirmPassword.text.toString().trim { it <= ' ' }.isEmpty()) {
            showToast("Confirm your password")
            false
        } else if (binding!!.inputPassword.text.toString() != binding!!.inputConfirmPassword.text.toString()) {
            showToast("Password & confirm password must match")
            false
        } else {
            true
        }

    private fun loading(isLoading: Boolean) {
        if (isLoading) {
            binding!!.buttonSignUp.visibility = View.INVISIBLE
            binding!!.progressBar.visibility = View.VISIBLE
        } else {
            binding!!.progressBar.visibility = View.INVISIBLE
            binding!!.buttonSignUp.visibility = View.VISIBLE
        }
    }
}