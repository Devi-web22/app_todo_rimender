package com.example.todorimender

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class register : AppCompatActivity() {

    private lateinit var dbHelper: databasehelper
    private lateinit var edtUsername: EditText
    private lateinit var edtPassword: EditText
    private lateinit var btnRegister: Button
    private lateinit var btnBackToLogin: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)


        dbHelper = databasehelper(this)
        edtUsername = findViewById(R.id.edtRegUsername)
        edtPassword = findViewById(R.id.edtRegPassword)
        btnRegister = findViewById(R.id.btnRegisterUser)
        btnBackToLogin = findViewById(R.id.btnBackToLogin)


        btnRegister.setOnClickListener {
            val username = edtUsername.text.toString().trim()
            val password = edtPassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Isi semua field", Toast.LENGTH_SHORT).show()
            } else {
                val success = dbHelper.registerUser(username, password)
                if (success) {
                    Toast.makeText(this, "Registrasi berhasil! Silakan login", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, login::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Username sudah ada!", Toast.LENGTH_SHORT).show()
                }
            }
        }


        btnBackToLogin.setOnClickListener {
            startActivity(Intent(this, login::class.java))
            finish()
        }
    }
}
