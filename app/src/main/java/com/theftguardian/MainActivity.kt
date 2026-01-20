package com.theftguardian

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.theftguardian.api.AgentContext
import com.theftguardian.api.AgentExecuteRequest
import com.theftguardian.api.AgentExecuteResponse
import com.theftguardian.api.Signal
import com.theftguardian.api.TheftAgentApiClient
import com.theftguardian.auth.LoginActivity
import com.theftguardian.utils.TokenManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var btnSimulateTheft: Button
    private lateinit var btnSimulateNormal: Button
    private lateinit var btnAdvancedSignals: Button
    private lateinit var tvResponse: TextView
    private lateinit var tvUserWelcome: TextView
    private lateinit var apiClient: TheftAgentApiClient
    private lateinit var tokenManager: TokenManager

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Check authentication
        tokenManager = TokenManager(this)
        if (!tokenManager.isLoggedIn()) {
            navigateToLogin()
            return
        }
        
        setContentView(R.layout.activity_main)

        // Initialize views
        btnSimulateTheft = findViewById(R.id.btnSimulateTheft)
        btnSimulateNormal = findViewById(R.id.btnSimulateNormal)
        btnAdvancedSignals = findViewById(R.id.btnAdvancedSignals)
        tvResponse = findViewById(R.id.tvResponse)
        tvUserWelcome = findViewById(R.id.tvUserWelcome)

        // Initialize API client with token
        apiClient = TheftAgentApiClient(tokenManager.getToken()!!)
        
        // Display welcome message
        val userName = tokenManager.getUserName() ?: "User"
        tvUserWelcome.text = "Welcome, $userName!"

        // Button click listeners
        btnSimulateTheft.setOnClickListener {
            val signals = arrayOf(
                "Power Off Attempt", "SIM Change", "Location Jump",
                "Face Lock Fail (3x)", "Wrong PIN", "Sudden Jerk", "Screen Toggle Quick"
            )
            // Sab items ko true set kar do (Select All)
            val checkedItems = BooleanArray(signals.size) { true }

            Toast.makeText(this, "🚀 Triggering FULL THEFT Scenario...", Toast.LENGTH_SHORT).show()
            tvResponse.text = "ALL SIGNALS TRIGGERED:\n" + signals.joinToString("\n") { ". $it" }
            sendAdvancedSignals(signals, checkedItems)
        }
        
        btnSimulateNormal.setOnClickListener {
           // simulateNormalEvent()
            val signals = arrayOf("Wrong PIN")
            val checkedItems = booleanArrayOf(true)
            Toast.makeText(this, "Simulating Normal Activity...", Toast.LENGTH_SHORT).show()
            sendAdvancedSignals(signals, checkedItems)
        }
        
        btnAdvancedSignals.setOnClickListener {
            showAdvancedSignalsDialog()
        }

        Log.d("TheftGuardian", "MainActivity initialized for user: $userName")
    }
    
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                logout()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
/*
    @SuppressLint("SetTextI18n")
    private fun simulateTheftEvent() {
        val deviceId = "DEMO_DEVICE_${android.os.Build.MODEL}"
        val eventType = "SUSPICIOUS_ACTIVITY"
        val confidenceScore = (75..95).random()
        val timestamp = System.currentTimeMillis()

        val request = TheftEventRequest(
            deviceId = deviceId,
            eventType = eventType,
            confidenceScore = confidenceScore,
            timestamp = timestamp
        )

        Log.d("TheftGuardian", "Sending theft event: $request")
        tvResponse.text = "⚠️ Simulating THEFT scenario...\nConfidence: $confidenceScore%"

        sendEventRequest(request, confidenceScore)
    }
    
    @SuppressLint("SetTextI18n")
    private fun simulateNormalEvent() {
        val deviceId = "DEMO_DEVICE_${android.os.Build.MODEL}"
        val eventType = "NORMAL_ACTIVITY"
        val confidenceScore = (20..60).random()
        val timestamp = System.currentTimeMillis()

        val request = TheftEventRequest(
            deviceId = deviceId,
            eventType = eventType,
            confidenceScore = confidenceScore,
            timestamp = timestamp
        )

        Log.d("TheftGuardian", "Sending normal event: $request")
        tvResponse.text = "✓ Simulating NORMAL scenario...\nConfidence: $confidenceScore%"

        sendEventRequest(request, confidenceScore)
    }*/
    
    private fun showAdvancedSignalsDialog() {
        val signals = arrayOf(
            "Power Off Attempt",
            "SIM Change",
            "Location Jump",
            "Face Lock Fail (3x)",
            "Wrong PIN",
            "Sudden Jerk",
            "Screen Toggle Quick"
        )
        
        val checkedItems = booleanArrayOf(false, false, false, false, false, false, false)
        
        AlertDialog.Builder(this)
            .setTitle("Select Signals to Send")
            .setMultiChoiceItems(signals, checkedItems) { _, which, isChecked ->
                checkedItems[which] = isChecked
            }
            .setPositiveButton("Send") { _, _ ->
                sendAdvancedSignals(signals, checkedItems)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    @SuppressLint("SetTextI18n")
    private fun sendAdvancedSignals(signals: Array<String>, checkedItems: BooleanArray) {
        val selectedSignals = mutableListOf<String>()
        for (i in signals.indices) {
            if (checkedItems[i]) {
                selectedSignals.add(signals[i])
            }
        }
        
        if (selectedSignals.isEmpty()) {
            Toast.makeText(this, "No signals selected", Toast.LENGTH_SHORT).show()
            return
        }
        
        tvResponse.text = "🔍 Processing ${selectedSignals.size} signal(s):\n" +
                selectedSignals.joinToString("\n") { "• $it" }
        
        // Map UI signal names to API signal types
        val signalTypeMap = mapOf(
            "Power Off Attempt" to "power_off_attempt",
            "SIM Change" to "sim_change",
            "Location Jump" to "location_jump",
            "Face Lock Fail (3x)" to "face_lock_fail",
            "Wrong PIN" to "wrong_pin",
            "Sudden Jerk" to "sudden_jerk",
            "Screen Toggle Quick" to "screen_on_off_quick"
        )
        
        // Build signal list for API
        val apiSignals = selectedSignals.mapNotNull { signalName ->
            signalTypeMap[signalName]?.let { type ->
                Signal(
                    type = type,
                    timestamp = System.currentTimeMillis(),
                    metadata = if (type == "face_lock_fail") mapOf("attemptCount" to 3) else null
                )
            }
        }
        
        // Build context
        val context = AgentContext(
            ownerName = tokenManager.getUserName() ?: "User",
            lastKnownLocation = "Unknown",
            batteryLevel = 85
        )
        
        val request = AgentExecuteRequest(
            signals = apiSignals,
            context = context
        )
        
        // Send to API
        sendAgentExecuteRequest(request)
    }
/*
    private fun sendEventRequest(request: TheftEventRequest, confidenceScore: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiClient.sendTheftEvent(request)

                withContext(Dispatchers.Main) {
                    displayResponse(response, confidenceScore)
                    Log.d("TheftGuardian", "Response received: $response")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    val errorMsg = "Error: ${e.message}"
                    tvResponse.text = errorMsg
                    Toast.makeText(this@MainActivity, errorMsg, Toast.LENGTH_SHORT).show()
                    Log.e("TheftGuardian", "API call failed", e)
                }
            }
        }
    }*/
    
    private fun sendAgentExecuteRequest(request: AgentExecuteRequest) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiClient.executeAgent(request)
                
                withContext(Dispatchers.Main) {
                    displayAgentResponse(response)
                    Log.d("TheftGuardian", "Agent response received: $response")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    val errorMsg = "Error: ${e.message}"
                    tvResponse.text = errorMsg
                    Toast.makeText(this@MainActivity, errorMsg, Toast.LENGTH_SHORT).show()
                    Log.e("TheftGuardian", "API call failed", e)
                }
            }
        }
    }
/*
    private fun displayResponse(response: TheftEventResponse, confidenceScore: Int) {
        val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        
        val displayText = buildString {
            appendLine("═══════════════════════════")
            appendLine("THEFT EVENT SIMULATION")
            appendLine("═══════════════════════════")
            appendLine("Time: $timestamp")
            appendLine("Confidence Score: $confidenceScore%")
            appendLine()
            appendLine("STATUS: ${response.status}")
            appendLine("MESSAGE: ${response.message}")
            appendLine()
            appendLine("ACTION PLAN:")
            response.actions.forEach { action ->
                appendLine("  → $action")
            }
            appendLine()
            appendLine("Severity: ${response.severity}")
            appendLine("═══════════════════════════")
        }
        
        tvResponse.text = displayText
        
        val toastMsg = if (response.actions.size > 1) {
            "⚠️ Theft detected! ${response.actions.size} actions triggered"
        } else {
            "✓ Normal activity"
        }
        Toast.makeText(this, toastMsg, Toast.LENGTH_LONG).show()
    }*/
    
    @SuppressLint("SetTextI18n")
    private fun displayAgentResponse(response: AgentExecuteResponse) {
        val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        
        val displayText = buildString {
            appendLine("═══════════════════════════")
            appendLine("AGENT EXECUTION RESULT")
            appendLine("═══════════════════════════")
            appendLine("Time: $timestamp")
            appendLine()
            appendLine("SUCCESS: ${response.success}")
            appendLine("STATE: ${response.state}")
            appendLine("SCORE: ${response.score}")
            appendLine()
            response.agentResponse?.let { agent ->
                appendLine("AGENT RESPONSE:")
                agent.id?.let { appendLine("  Task ID: $it") }
                agent.streamUrl?.let { appendLine("  Stream URL: $it") }
                agent.token?.let { appendLine("  Token: ${it.take(20)}...") }
            }
            appendLine("═══════════════════════════")
        }
        
        tvResponse.text = displayText
        
        val toastMsg = if (response.state == "THEFT_MODE") {
            "⚠️ THEFT DETECTED! Score: ${response.score}"
        } else {
            "✓ ${response.state} - Score: ${response.score}"
        }
        Toast.makeText(this, toastMsg, Toast.LENGTH_LONG).show()
    }
    
    private fun logout() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { _, _ ->
                tokenManager.clearAll()
                navigateToLogin()
            }
            .setNegativeButton("No", null)
            .show()
    }
    
    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
