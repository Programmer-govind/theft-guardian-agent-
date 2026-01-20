package com.theftguardian.api

import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

// Data classes for API
data class User(
    val id: String,
    val email: String,
    val name: String
)

data class LoginRequest(val email: String, val password: String)

data class RegisterRequest(
    val email: String,
    val name: String,
    val password: String,
    val deviceId: String? = null,
    val mobileRunApiKey: String? = null
)

data class ApiResponse(
    val success: Boolean,
    val message: String,
    val token: String? = null,
    val user: User? = null
)

class AuthApiClient {
    // Base Url
    private val baseUrl = "https://0cmpj6zr-3000.inc1.devtunnels.ms"

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    // REAL LOGIN (MOCK REMOVED)
    fun login(loginRequest: LoginRequest): ApiResponse {
        val requestBody = gson.toJson(loginRequest).toRequestBody(jsonMediaType)
        val request = Request.Builder()
            .url("$baseUrl/auth/login")
            .post(requestBody)
            .build()

        return try {
            client.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: ""
                gson.fromJson(body, ApiResponse::class.java)
            }
        } catch (e: Exception) {
            ApiResponse(false, "Network error: ${e.message}")
        }
    }

    // REAL REGISTER (MOCK REMOVED)
    fun register(regRequest: RegisterRequest): ApiResponse {
        val requestBody = gson.toJson(regRequest).toRequestBody(jsonMediaType)
        val request = Request.Builder()
            .url("$baseUrl/auth/register")
            .post(requestBody)
            .build()

        return try {
            client.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: ""
                gson.fromJson(body, ApiResponse::class.java)
            }
        } catch (e: Exception) {
            ApiResponse(false, "Network error: ${e.message}")
        }
    }
}