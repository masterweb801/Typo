package com.logicrealm.typo

import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException
import android.content.Context
import android.widget.ImageButton
import android.widget.LinearLayout
import android.graphics.Rect
import android.view.ViewGroup

class MainActivity : AppCompatActivity() {
    private lateinit var edtMessage: EditText
    private lateinit var btnSend: Button
    private lateinit var edtIpAddress: EditText
    private lateinit var btnSave: Button
    private lateinit var btnReturn: ImageButton
    private lateinit var rootLayout: LinearLayout
    private lateinit var buttonsLayout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        rootLayout = findViewById(R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(rootLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        edtMessage = findViewById(R.id.edtMessage)
        btnSend = findViewById(R.id.btnSend)
        edtIpAddress = findViewById(R.id.edtIpAddress)
        btnSave = findViewById(R.id.btnSave)
        btnReturn = findViewById(R.id.btnReturn)

        val sharedPreferences = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val savedIp = sharedPreferences.getString("ip_address", "")
        edtIpAddress.setText(savedIp)

        btnSave.setOnClickListener {
            val ip = edtIpAddress.text.toString().trim()
            if (ip.isNotEmpty()) {
                val editor = sharedPreferences.edit()
                editor.putString("ip_address", ip)
                editor.apply()
                Toast.makeText(this, "IP Address saved", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Please enter a valid IP Address", Toast.LENGTH_SHORT).show()
            }
        }

        btnSend.setOnClickListener {
            val message = edtMessage.text.toString()
            val ip = edtIpAddress.text.toString().trim()
            if (message.isNotEmpty() && ip.isNotEmpty()) {
                sendPostRequest(message, ip)
            } else if (message.isEmpty()) {
                Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show()
            } else if (ip.isEmpty()) {
                Toast.makeText(this, "Please enter IP Address", Toast.LENGTH_SHORT).show()
            }
        }

        btnReturn.setOnClickListener {
            val ip = edtIpAddress.text.toString().trim()
            if (ip.isNotEmpty()) {
                sendPostRequest("<-RETURN->", ip)
            }
        }


        edtMessage.requestFocus()
        edtMessage.setOnEditorActionListener(TextView.OnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val message = edtMessage.text.toString()
                val ip = edtIpAddress.text.toString().trim()
                if (message.isNotEmpty() && ip.isNotEmpty()) {
                    sendPostRequest(message, ip)
                } else if (message.isEmpty()) {
                    Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show()
                } else if (ip.isEmpty()) {
                    Toast.makeText(this, "Please enter IP Address", Toast.LENGTH_SHORT).show()
                }
                return@OnEditorActionListener true
            }
            false
        })

        buttonsLayout = findViewById(R.id.buttonsLayout)

        rootLayout.viewTreeObserver.addOnGlobalLayoutListener {
            val rect = Rect()
            rootLayout.getWindowVisibleDisplayFrame(rect)
            val screenHeight = rootLayout.rootView.height
            val keypadHeight = screenHeight - rect.bottom

            if (keypadHeight > screenHeight * 0.15) {
                val params = buttonsLayout.layoutParams as LinearLayout.LayoutParams
                params.bottomMargin = keypadHeight - 150
                buttonsLayout.layoutParams = params
                val params2 = edtMessage.layoutParams as ViewGroup.MarginLayoutParams
                params2.bottomMargin = 10
                edtMessage.layoutParams = params2
            } else {
                val params = buttonsLayout.layoutParams as LinearLayout.LayoutParams
                params.bottomMargin = 0
                buttonsLayout.layoutParams = params
                val params2 = edtMessage.layoutParams as ViewGroup.MarginLayoutParams
                params2.bottomMargin = 30
                edtMessage.layoutParams = params2
            }
        }
    }

    private fun sendPostRequest(message: String, ip: String) {
        val url = "http://$ip:6969/send_message"
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