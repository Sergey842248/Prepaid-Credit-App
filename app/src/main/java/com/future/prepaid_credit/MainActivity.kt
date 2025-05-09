package com.example.prepaidcredit

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.textfield.TextInputEditText

class MainActivity : AppCompatActivity() {

    private lateinit var providerDropdown: AutoCompleteTextView
    private lateinit var codeEditText: TextInputEditText
    private lateinit var rechargeButton: Button
    private var selectedProvider: String = "Telekom" // Default provider

    private val CALL_PHONE_PERMISSION_REQUEST = 101

    // Map of providers to their USSD codes
    private val providerUssdCodes = mapOf(
        "Aldi Talk" to "*104*%s%23",
        "Blau" to "*103*%s%23",
        "Congstar" to "*101*%s%23",
        "klarmobil" to "*101*%s%23",
        "Lidl Connect" to "*100*%s%23",
        "Lyca Mobile" to "*131*%s%23",
        "NORMA Connect" to "*101*%s%23",
        "O2" to "*103*%s%23",
        "Ortel Mobile" to "*103*%s%23",
        "otelo" to "*100*%s%23",
        "Telekom" to "*101*%s%23",
        "Vodafone" to "*100*%s%23",
    )

    // Constants for SharedPreferences
    private val PREFS_NAME = "PrepaidCreditPrefs"
    private val KEY_SELECTED_PROVIDER = "selectedProvider"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize views
        providerDropdown = findViewById(R.id.providerDropdown)
        codeEditText = findViewById(R.id.codeEditText)
        rechargeButton = findViewById(R.id.rechargeButton)

        // Load saved provider preference
        loadSavedProvider()

        // Set up the provider dropdown
        setupProviderDropdown()

        rechargeButton.setOnClickListener {
            val code = codeEditText.text.toString().trim()
            if (code.isEmpty()) {
                Toast.makeText(this, "Please type your Top Up code", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (checkCallPhonePermission()) {
                dialUssdCode(code)
            } else {
                requestCallPhonePermission()
            }
        }
    }

    private fun loadSavedProvider() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        selectedProvider = prefs.getString(KEY_SELECTED_PROVIDER, "Telekom") ?: "Telekom"
    }

    private fun saveSelectedProvider() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putString(KEY_SELECTED_PROVIDER, selectedProvider)
        editor.apply()
    }

    private fun setupProviderDropdown() {
        val providers = resources.getStringArray(R.array.providers)
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, providers)
        providerDropdown.setAdapter(adapter)
        
        // Set saved selection
        providerDropdown.setText(selectedProvider, false)
        
        providerDropdown.setOnItemClickListener { _, _, position, _ ->
            selectedProvider = providers[position]
            saveSelectedProvider() // Save the selection when changed
        }
    }

    private fun checkCallPhonePermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CALL_PHONE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCallPhonePermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CALL_PHONE),
            CALL_PHONE_PERMISSION_REQUEST
        )
    }

    private fun dialUssdCode(code: String) {
        try {
            // Get the USSD code format for the selected provider
            val ussdCodeFormat = providerUssdCodes[selectedProvider] ?: "*100*%s%23"
            
            // Format the USSD code with the recharge code
            val ussdCode = String.format(ussdCodeFormat, code)
            
            val uri = Uri.parse("tel:$ussdCode")
            val intent = Intent(Intent.ACTION_CALL, uri)
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Error at choosing: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == CALL_PHONE_PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                val code = codeEditText.text.toString().trim()
                if (code.isNotEmpty()) {
                    dialUssdCode(code)
                }
            } else {
                Toast.makeText(
                    this,
                    "Calling permission is required to make a call",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}