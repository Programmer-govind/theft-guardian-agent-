package com.theftguardian.auth

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.theftguardian.MainActivity
import com.theftguardian.R
import com.theftguardian.api.AuthApiClient
import com.theftguardian.api.LoginRequest
import com.theftguardian.utils.TokenManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var tvRegister: TextView
    private lateinit var apiClient: AuthApiClient
    private lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if already logged in
        tokenManager = TokenManager(this)
        if (tokenManager.getToken() != null) {
            navigateToMain()
            return
        }

        setContentView(R.layout.activity_login)

        // Initialize views
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvRegister = findViewById(R.id.tvRegister)

        // Initialize API client
        apiClient = AuthApiClient()

        // Login button click
        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (validateInput(email, password)) {
                performLogin(email, password)
            }
        }

        // Register link click
        tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        Log.d("TheftGuardian", "LoginActivity initialized")
    }

    private fun validateInput(email: String, password: String): Boolean {
        if (email.isEmpty()) {
            Toast.makeText(this, "Please enter email", Toast.LENGTH_SHORT).show()
            return false
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter valid email", Toast.LENGTH_SHORT).show()
            return false
        }

        if (password.isEmpty()) {
            Toast.makeText(this, "Please enter password", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    @SuppressLint("SetTextI18n")
    private fun performLogin(email: String, password: String) {
        btnLogin.isEnabled = false
        btnLogin.text = "Logging in..."

        val request = LoginRequest(email, password)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiClient.login(request)

                withContext(Dispatchers.Main) {
                    if (response.token != null && response.user != null) {
                        // SUCCESS
                        tokenManager.saveToken(response.token)
                        tokenManager.saveUserInfo(
                            response.user.id.toLongOrNull() ?: 0L,
                            response.user.email,
                            response.user.name
                        )
                        Toast.makeText(this@LoginActivity, "Welcome, ${response.user.name}!", Toast.LENGTH_SHORT).show()
                        navigateToMain()
                    } else {
                        // SAFE ERROR HANDLING
                        val serverMessage = response.message

                        // Ise simplified 'when' kehte hain
                        val errorMsg = when {
                            serverMessage == null -> "Invalid Credentials"
                            // ?. use karne se agar message null hua toh crash nahi hoga
                            serverMessage.contains("credentials", ignoreCase = true) -> "Invalid Email or Password"
                            serverMessage.contains("found", ignoreCase = true) -> "User not found"
                            else -> serverMessage
                        }
                        showError(errorMsg)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    // NETWORK OR CONNECTION ERROR
                    val networkError = when {
                        e is java.net.ConnectException -> "Cannot connect to server. Is it running?"
                        e is java.net.SocketTimeoutException -> "Server timed out. Try again."
                        e.message?.contains("404") == true -> "Endpoint not found (404)"
                        else -> "Network Error: ${e.localizedMessage ?: "Check Internet"}"
                    }
                    showError(networkError)
                }
            }
        }
    }

    // Helper function to reset button and show toast safely
    @SuppressLint("SetTextI18n")
    private fun showError(message: String) {
        Toast.makeText(this@LoginActivity, message, Toast.LENGTH_LONG).show()
        btnLogin.isEnabled = true
        btnLogin.text = "LOGIN"
        Log.e("TheftGuardian", "Login Error Displayed: $message")
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
