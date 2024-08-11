package com.afivadnan.y

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.afivadnan.y.databinding.ActivityAddFriendBinding
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.Base64

class AddFriend : AppCompatActivity() {

    private lateinit var binding: ActivityAddFriendBinding
    private lateinit var viewModel: FriendViewModel
    private var friendId: Int? = null
    private var photoPath: String = ""
    private lateinit var photoFile: File

    @RequiresApi(Build.VERSION_CODES.O)
    private var galleryLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                val parcelFileDescriptor = contentResolver.openFileDescriptor(
                    it.data?.data ?: return@registerForActivityResult, "r"
                )
                val fileDescriptor = parcelFileDescriptor?.fileDescriptor
                val inputStream = FileInputStream(fileDescriptor)
                val outputStream = FileOutputStream(photoFile)

                inputStream.use { input ->
                    outputStream.use { output ->
                        input.copyTo(output)
                    }
                }

                parcelFileDescriptor?.close()
                val takenImage = BitmapFactory.decodeFile(photoFile.absolutePath)
                binding.addImg.setImageBitmap(takenImage)
                photoPath = bitmapToStr(takenImage)
            }
        }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_friend)
        enableEdgeToEdge()

        initView()

        photoFile = try {
            createImageFile()
        } catch (ex: IOException) {
            Toast.makeText(this, "Error creating image file", Toast.LENGTH_SHORT).show()
            return
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createImageFile(): File {
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val file = File.createTempFile("photo", ".jpg", storageDir)
        Log.d("AddFriend", "Photo File Path: ${file.absolutePath}")
        return file
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initView() {
        val factoryModel = Factory(this)
        viewModel = ViewModelProvider(this, factoryModel)[FriendViewModel::class.java]

        friendId = intent.getIntExtra("FRIEND_ID", -1)

        if (friendId != -1) {
            lifecycleScope.launch {
                viewModel.getFriend().collect { friends ->
                    val friend = friends.firstOrNull { it.id == friendId }
                    if (friend != null) {
                        binding.adNama.setText(friend.name)
                        binding.adSekolah.setText(friend.school)
                        binding.adHobi.setText(friend.hobby)

                        val photoBitmap = strToBitmap(friend.photo)
                        binding.addImg.setImageBitmap(photoBitmap)
                        photoPath = friend.photo
                    }
                }
            }
            binding.btnSv.visibility = View.GONE
            binding.updateButton.visibility = View.VISIBLE
            binding.deleteButton.visibility = View.VISIBLE
        } else {
            binding.btnSv.visibility = View.VISIBLE
            binding.updateButton.visibility = View.GONE
            binding.deleteButton.visibility = View.GONE
        }

        binding.btnSv.setOnClickListener {
            saveFriend()
        }

        binding.updateButton.setOnClickListener {
            updateFriend()
        }

        binding.deleteButton.setOnClickListener {
            deleteFriend()
        }

        binding.addImg.setOnClickListener {
            openGallery()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun openGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(galleryIntent)
    }

    private fun saveFriend() {
        val name = binding.adNama.text.toString()
        val school = binding.adSekolah.text.toString()
        val hobby = binding.adHobi.text.toString()

        if (name.isBlank() || school.isBlank() || hobby.isBlank()) {
            Toast.makeText(this, "All fields must be filled", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val friend = Friend(name = name, school = school, hobby = hobby, photo = photoPath)
            lifecycleScope.launch {
                viewModel.insertFriend(friend)
                setResult(RESULT_OK)
                finish()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to save friend: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateFriend() {
        val name = binding.adNama.text.toString()
        val school = binding.adSekolah.text.toString()
        val hobby = binding.adHobi.text.toString()

        if (name.isBlank() || school.isBlank() || hobby.isBlank()) {
            Toast.makeText(this, "All fields must be filled", Toast.LENGTH_SHORT).show()
            return
        }

        val friend = Friend(name = name, school = school, hobby = hobby, photo = photoPath).apply {
            id = friendId ?: 0
        }
        lifecycleScope.launch {
            viewModel.updateFriend(friend)
            setResult(RESULT_OK)
            finish()
        }
    }

    private fun deleteFriend() {
        val friend = Friend(name = "", school = "", hobby = "", photo = "").apply {
            id = friendId ?: 0
        }
        lifecycleScope.launch {
            viewModel.deleteFriend(friend)
            setResult(RESULT_OK)
            finish()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun bitmapToStr(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.getEncoder().encodeToString(byteArray)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun strToBitmap(encodedString: String): Bitmap? {
        return try {
            val cleanedString = encodedString.replace("\\s".toRegex(), "")
            val byteArray = Base64.getDecoder().decode(cleanedString)
            BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
