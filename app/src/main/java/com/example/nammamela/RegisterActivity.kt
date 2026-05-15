package com.example.nammamela

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import android.util.Log
import androidx.room.Room
import com.example.nammamela.data.local.AppDatabase
import com.example.nammamela.data.local.User
import com.example.nammamela.ui.AppViewModel

class RegisterActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "seat_db"
        ).fallbackToDestructiveMigration().build()

        val viewModel = ViewModelProvider(this)[AppViewModel::class.java]

        val etUsername = findViewById<EditText>(R.id.etRegUsername)
        val etEmail = findViewById<EditText>(R.id.etRegEmail)
        val etPhone = findViewById<EditText>(R.id.etRegPhone)
        val etPassword = findViewById<EditText>(R.id.etRegPassword)
        val etConfirmPassword = findViewById<EditText>(R.id.etRegConfirmPassword)
        val etOtp = findViewById<EditText>(R.id.etOtp)
        val btnSendOtp = findViewById<Button>(R.id.btnSendOtp)
        val btnRegister = findViewById<Button>(R.id.btnRegister)

        btnSendOtp.setOnClickListener {
            viewModel.sendOtp { otp ->
                AlertDialog.Builder(this)
                    .setTitle("Simulated OTP")
                    .setMessage("Your OTP is: $otp")
                    .setPositiveButton("OK", null)
                    .show()
            }
        }

        btnRegister.setOnClickListener {
            Thread {
                val u = etUsername.text.toString().trim()
                val e = etEmail.text.toString().trim()
                val ph = etPhone.text.toString().trim()
                val p = etPassword.text.toString().trim()
                val cp = etConfirmPassword.text.toString().trim()
                val otp = etOtp.text.toString().trim()

                if (u.isEmpty() || e.isEmpty() || ph.isEmpty() || p.isEmpty() || cp.isEmpty() || otp.isEmpty()) {
                    runOnUiThread {
                        Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show()
                    }
                    return@Thread
                }

                if (p != cp) {
                    runOnUiThread {
                        Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                    }
                    return@Thread
                }

                val existingUser = db.userDao().getUser(u)
                if (existingUser != null) {
                    runOnUiThread {
                        Toast.makeText(this, "Username already exists", Toast.LENGTH_SHORT).show()
                    }
                    return@Thread
                }

                val newUser = User(
                    username = u,
                    email = e,
                    phoneNumber = ph,
                    password = p,
                    role = "user"
                )

                db.userDao().insertUser(newUser)

                Log.d("REGISTER_DEBUG", "Inserted User = $newUser")
                Log.d("DB_USERS", db.userDao().getAllUsers().toString())

                runOnUiThread {
                    Toast.makeText(this, "Registration Successful", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }.start()
        }
    }
}
