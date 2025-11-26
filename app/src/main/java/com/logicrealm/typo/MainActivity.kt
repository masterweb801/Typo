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
import android.widget.ImageButton
import android.widget.LinearLayout
import android.graphics.Rect
import android.view.ViewGroup
import androidx.core.content.edit
import com.logicrealm.typo.utils.NetworkScanner
import io.github.cdimascio.dotenv.dotenv
import android.view.View
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MainActivity : AppCompatActivity() {
    private lateinit var edtMessage: EditText
    private lateinit var btnSend: Button
    private lateinit var edtIpAddress: EditText
    private lateinit var btnSave: Button
    private lateinit var btnReturn: ImageButton
    private lateinit var rootLayout: LinearLayout
    private lateinit var buttonsLayout: LinearLayout
    private lateinit var btnConnect: Button
    private lateinit var btnBkSpace: ImageButton
    private lateinit var niLayout: LinearLayout
    private lateinit var mainLayout: LinearLayout
    private lateinit var btnRefresh: Button


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

        niLayout = findViewById(R.id.niLayout)
        mainLayout = findViewById(R.id.mainLayout)
        buttonsLayout = findViewById(R.id.buttonsLayout)

        edtMessage = findViewById(R.id.edtMessage)
        btnSend = findViewById(R.id.btnSend)
        edtIpAddress = findViewById(R.id.edtIpAddress)
        btnSave = findViewById(R.id.btnSave)
        btnReturn = findViewById(R.id.btnReturn)
        btnConnect = findViewById(R.id.btnCon)
        btnBkSpace = findViewById(R.id.btnBkSpace)
        btnRefresh = findViewById(R.id.btnRefresh)


        val sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        val savedIp = sharedPreferences.getString("ip_address", "")
        edtIpAddress.setText(savedIp)

        val dotenv = dotenv {
            directory = "./assets"
            filename = "env"
        }
        val port = dotenv["PORT"]?.toIntOrNull()?:throw IllegalArgumentException("PORT is missing or malformed in .env file. App cannot start.")
        val auth = dotenv["SECRET"] ?:throw IllegalArgumentException("SECRET is missing or malformed in .env file. App cannot start.")

        checkConnectivity()

        btnRefresh.setOnClickListener {
            checkConnectivity()
        }

        val subnet = NetworkScanner.getLocalSubnet()

        btnSave.setOnClickListener {
            val ip = edtIpAddress.text.toString().trim()
            if (ip.isNotEmpty()) {
                sharedPreferences.edit {
                    putString("ip_address", ip)
                }
                Toast.makeText(this, "IP Address saved", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Please enter a valid IP Address", Toast.LENGTH_SHORT).show()
            }
        }

        btnConnect.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                if (subnet !== null) {
                    val connectedHost = NetworkScanner.scanLocalNetwork(subnet, port).firstOrNull()
                    withContext(Dispatchers.Main) {
                        if (connectedHost !== null) {
                            edtIpAddress.setText(connectedHost)
                            Toast.makeText(this@MainActivity, "Remote device found!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@MainActivity, "No device found!", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, "No internet connection!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        btnSend.setOnClickListener {
            val message = edtMessage.text.toString()
            val ip = edtIpAddress.text.toString().trim()
            if (message.isNotEmpty() && ip.isNotEmpty()) {
                sendPostRequest(message, ip, port.toString(), auth)
            } else if (message.isEmpty()) {
                Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show()
            } else if (ip.isEmpty()) {
                Toast.makeText(this, "Please enter IP Address", Toast.LENGTH_SHORT).show()
            }
        }

        btnReturn.setOnClickListener {
            val ip = edtIpAddress.text.toString().trim()
            if (ip.isNotEmpty()) {
                sendPostRequest("<-RETURN->", ip, port.toString(), auth)
            }
        }

         btnBkSpace.setOnClickListener {
            val ip = edtIpAddress.text.toString().trim()
            if (ip.isNotEmpty()) {
                sendPostRequest("<-BACKSPACE->", ip, port.toString(), auth)
            }
        }


        edtMessage.requestFocus()
        edtMessage.setOnEditorActionListener(TextView.OnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val message = edtMessage.text.toString()
                val ip = edtIpAddress.text.toString().trim()
                if (message.isNotEmpty() && ip.isNotEmpty()) {
                    sendPostRequest(message, ip, port.toString(), auth)
                } else if (message.isEmpty()) {
                    Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show()
                } else if (ip.isEmpty()) {
                    Toast.makeText(this, "Please enter IP Address", Toast.LENGTH_SHORT).show()
                }
                return@OnEditorActionListener true
            }
            false
        })

        rootLayout.viewTreeObserver.addOnGlobalLayoutListener {
            val rect = Rect()
            rootLayout.getWindowVisibleDisplayFrame(rect)
            val screenHeight = rootLayout.rootView.height
            val keypadHeight = screenHeight - rect.bottom

            if (keypadHeight > screenHeight * 0.15) {
                val params = buttonsLayout.layoutParams as LinearLayout.LayoutParams
                params.bottomMargin = keypadHeight - 120
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

    private fun checkConnectivity() {
        if (NetworkScanner.isDeviceOnline(this)) {
            mainLayout.visibility = View.VISIBLE
            niLayout.visibility = View.GONE
        } else {
            mainLayout.visibility = View.GONE
            niLayout.visibility = View.VISIBLE
        }
    }

    private fun sendPostRequest(message: String, ip: String, port: String, auth: String) {
        val url = "http://$ip:$port/send_message"
        val client = OkHttpClient()

        val json = """{"message": "$message", "auth": "$auth"}"""

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