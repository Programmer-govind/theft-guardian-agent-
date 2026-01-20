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
import com.theftguardian.R
import com.theftguardian.api.AuthApiClient
import com.theftguardian.api.RegisterRequest
import com.theftguardian.utils.TokenManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegisterActivity : AppCompatActivity() {

    private lateinit var etName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnRegister: Button
    private lateinit var tvLogin: TextView
    private lateinit var apiClient: AuthApiClient
    private lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Initialize views
        etName = findViewById(R.id.etName)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        btnRegister = findViewById(R.id.btnRegister)
        tvLogin = findViewById(R.id.tvLogin)

        // Initialize API client
        apiClient = AuthApiClient()
        tokenManager = TokenManager(this)

        // Register button click
        btnRegister.setOnClickListener {
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()

            if (validateInput(name, email, password, confirmPassword)) {
                performRegister(name, email, password)
            }
        }

        // Login link click
        tvLogin.setOnClickListener {
            finish()
        }

        Log.d("TheftGuardian", "RegisterActivity initialized")
    }

    private fun validateInput(
        name: String,
        email: String,
        password: String,
        confirmPassword: String
    ): Boolean {
        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show()
            return false
        }

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

        if (password.length < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
            return false
        }

        if (password != confirmPassword) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    @SuppressLint("SetTextI18n")
    private fun performRegister(name: String, email: String, password: String) {
        btnRegister.isEnabled = false
        btnRegister.text = "Creating account..."

        // Get device ID
        val deviceId = "ANDROID_${android.os.Build.MODEL}_${System.currentTimeMillis()}"

        val request = RegisterRequest(email, name, password, deviceId)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiClient.register(request)

                withContext(Dispatchers.Main) {
                    // Check if registration was successful
                    val token = response.token
                    if (token != null && response.user != null) {
                        // Save token and user info
                        tokenManager.saveToken(token)
                        tokenManager.saveUserInfo(
                            response.user.id.toLongOrNull() ?: 0L,
                            response.user.email,
                            response.user.name
                        )

                        Toast.makeText(
                            this@RegisterActivity,
                            "Account created successfully!",
                            Toast.LENGTH_SHORT
                        ).show()

                        Log.d("TheftGuardian", "Registration successful")

                        // Navigate to main
                        val intent = Intent(this@RegisterActivity, com.theftguardian.MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    } else {
                        // Handle case where API returns success:false or error message
                        Toast.makeText(
                            this@RegisterActivity,
                            response.message,
                            Toast.LENGTH_LONG
                        ).show()

                        btnRegister.isEnabled = true
                        btnRegister.text = "CREATE ACCOUNT"
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@RegisterActivity,
                        "Registration failed: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()

                    Log.e("TheftGuardian", "Registration error", e)

                    btnRegister.isEnabled = true
                    btnRegister.text = "CREATE ACCOUNT"
                }
            }
        }
    } // End of performRegister
} // End of RegisterActivity