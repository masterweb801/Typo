package com.logicrealm.typo

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import android.view.inputmethod.EditorInfo
import android.widget.TextView

class MainActivity : AppCompatActivity() {
    private lateinit var edtMessage: EditText
    private lateinit var btnSend: Button
    private lateinit var btnSave: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        edtMessage = findViewById(R.id.edtMessage)
        btnSend = findViewById(R.id.btnSend)
        btnSave = findViewById(R.id.btnSave)

        btnSend.setOnClickListener {
            val message = edtMessage.text.toString()
            if (message.isNotEmpty()) {
                sendPostRequest(message)
            } else {
                Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show()
            }
        }

        edtMessage.setOnEditorActionListener(TextView.OnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val message = edtMessage.text.toString().trim()
                if (message.isNotEmpty()) {
                    sendPostRequest(message)
                }
                return@OnEditorActionListener true
            }
            false
        })
    }

    private fun sendPostRequest(message: String) {
        val url = "http://192.168.0.103:6969/send_message"
        val client = OkHttpClient()

        val json = """{"message": "$message"}"""

        val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
        val requestBody = json.toRequestBody(mediaType)

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(applicationContext, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    runOnUiThread {
                        Toast.makeText(applicationContext, "Message Sent Successfully", Toast.LENGTH_SHORT).show()
                        edtMessage.text.clear()
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(applicationContext, "Failed to send message", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }
}