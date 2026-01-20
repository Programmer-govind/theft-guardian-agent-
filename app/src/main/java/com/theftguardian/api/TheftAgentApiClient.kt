package com.theftguardian.api

import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

// --- Models for API ---

// Signal model for /agent/execute endpoint
data class Signal(
    val type: String,
    val timestamp: Long,
    val metadata: Map<String, Any>? = null
)

data class AgentContext(
    val ownerName: String? = null,
    val lastKnownLocation: String? = null,
    val batteryLevel: Int? = null
)

data class AgentExecuteRequest(
    val signals: List<Signal>,
    val context: AgentContext? = null
)

data class AgentResponse(
    val id: String? = null,
    val streamUrl: String? = null,
    val token: String? = null
)

data class AgentExecuteResponse(
    val success: Boolean,
    val state: String,
    val score: Int,
    val agentResponse: AgentResponse? = null
)

// Legacy models for backward compatibility
/*
data class TheftEventRequest(
    val deviceId: String,
    val eventType: String,
    val confidenceScore: Int,
    val timestamp: Long
)

data class TheftEventResponse(
    val status: String,
    val message: String,
    val severity: String,
    val actions: List<String>
)
*/
// --- Client Class ---
class TheftAgentApiClient(private val token: String? = null) {

    private val baseUrl = "https://0cmpj6zr-3000.inc1.devtunnels.ms"
    private val gson = Gson()
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    // 1. Login
    fun login(loginRequest: LoginRequest): ApiResponse {
        val requestBody = gson.toJson(loginRequest).toRequestBody(jsonMediaType)
        val request = Request.Builder()
            .url("$baseUrl/auth/login")
            .post(requestBody)
            .build()
        return executeCall(request)
    }

    // 2. Register
    fun register(regRequest: RegisterRequest): ApiResponse {
        val requestBody = gson.toJson(regRequest).toRequestBody(jsonMediaType)
        val request = Request.Builder()
            .url("$baseUrl/auth/register")
            .post(requestBody)
            .build()
        return executeCall(request)
    }

    // 3. Execute Agent (Updated with Coroutines to fix Timeout/Blocking issues)
    suspend fun executeAgent(executeRequest: AgentExecuteRequest): AgentExecuteResponse = withContext(Dispatchers.IO) {
        val requestBody = gson.toJson(executeRequest).toRequestBody(jsonMediaType)
        val request = Request.Builder()
            .url("$baseUrl/agent/execute")
            .addHeader("Authorization", "Bearer $token")
            .post(requestBody)
            .build()

        try {
            client.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: ""
                android.util.Log.d("TheftGuardianAPI", "Status Code: ${response.code}")
                android.util.Log.d("TheftGuardianAPI", "Raw JSON: $body")

                if (response.isSuccessful) {
                    gson.fromJson(body, AgentExecuteResponse::class.java)
                } else {
                    // Server error handle karo
                    AgentExecuteResponse(false, "SERVER_ERROR_${response.code}", 0, null)
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("TheftGuardianAPI", "Call Failed: ${e.message}")
            AgentExecuteResponse(false, "NETWORK_ERROR", 0, null)
        }
    }

    // 4. Legacy Send Theft Event - Warning Fix
    /*
    @Deprecated("Use executeAgent instead", ReplaceWith("executeAgent()"))
    suspend fun sendTheftEvent(eventRequest: TheftEventRequest): TheftEventResponse =
        withContext(Dispatchers.IO) {
        val requestBody = gson.toJson(eventRequest).toRequestBody(jsonMediaType)
        val request = Request.Builder()
            .url("$baseUrl/agent/event")
            .addHeader("Authorization", "Bearer $token")
            .post(requestBody)
            .build()

        try {
            client.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: ""
                if (response.isSuccessful) {
                    gson.fromJson(body, TheftEventResponse::class.java)
                } else {
                    TheftEventResponse("ERROR", "Server returned ${response.code}", "HIGH", listOf("Check Connection"))
                }
            }
        } catch (e: Exception) {
            TheftEventResponse("ERROR", "Connection failed: ${e.message}", "HIGH", listOf("Check Internet"))
        }
    }*/

    private fun executeCall(request: Request): ApiResponse {
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