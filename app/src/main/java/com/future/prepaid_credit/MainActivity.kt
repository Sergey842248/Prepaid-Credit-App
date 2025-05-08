package com.example.prepaidcredit

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.textfield.TextInputEditText

class MainActivity : AppCompatActivity() {

    private lateinit var codeEditText: TextInputEditText
    private lateinit var rechargeButton: Button

    private val CALL_PHONE_PERMISSION_REQUEST = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        codeEditText = findViewById(R.id.codeEditText)
        rechargeButton = findViewById(R.id.rechargeButton)

        rechargeButton.setOnClickListener {
            val code = codeEditText.text.toString().trim()
            if (code.isEmpty()) {
                Toast.makeText(this, "Bitte geben Sie einen Aufladecode ein", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (checkCallPhonePermission()) {
                dialUssdCode(code)
            } else {
                requestCallPhonePermission()
            }
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
            // Format the USSD code with proper URI encoding for the # character
            val ussdCode = "*100*$code%23"  // %23 is the URL encoding for #
            val uri = Uri.parse("tel:$ussdCode")
            val intent = Intent(Intent.ACTION_CALL, uri)
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Fehler beim Wählen: ${e.message}", Toast.LENGTH_SHORT).show()
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
                    "Telefonberechtigung wird benötigt, um den Aufladecode zu wählen",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}