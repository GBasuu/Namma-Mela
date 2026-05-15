package com.example.nammamela

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.room.Room
import com.example.nammamela.data.local.AppDatabase
import com.example.nammamela.data.local.User

class LoginActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1G) AUTO LOGIN
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val loggedUser = prefs.getString("logged_user", null)
        val loggedRole = prefs.getString("logged_role", null)
        if (loggedUser != null) {
            val intent = if (loggedRole == "manager") {
                Intent(this, ManagerActivity::class.java)
            } else {
                Intent(this, MainActivity::class.java)
            }
            startActivity(intent)
            finish()
            return
        }

        setContentView(R.layout.activity_login)

        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "seat_db"
        ).fallbackToDestructiveMigration().build()

        val etUsername = findViewById<EditText>(R.id.etUsername)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnRegister = findViewById<Button>(R.id.btnGoToRegister)

        // 1C) INSERT DEFAULT USERS
        Thread {
            db.userDao().insertUser(
                User(
                    username = "admin",
                    email = "admin@gmail.com",
                    phoneNumber = "9999999999",
                    password = "1234",
                    role = "user"
                )
            )

            db.userDao().insertUser(
                User(
                    username = "manager",
                    email = "manager@gmail.com",
                    phoneNumber = "8888888888",
                    password = "admin123",
                    role = "manager"
                )
            )
            Log.d("LOGIN_DEBUG", "Default users inserted")
        }.start()

        btnLogin.setOnClickListener {
            Thread {
                val username = etUsername.text.toString().trim()
                val password = etPassword.text.toString().trim()

                val user = db.userDao().getUser(username)

                Log.d("LOGIN_DEBUG", "Entered Username = $username")
                Log.d("LOGIN_DEBUG", "DB User = $user")

                runOnUiThread {
                    when {
                        username.isEmpty() -> {
                            Toast.makeText(this, "Enter Username", Toast.LENGTH_SHORT).show()
                        }

                        password.isEmpty() -> {
                            Toast.makeText(this, "Enter Password", Toast.LENGTH_SHORT).show()
                        }

                        user == null -> {
                            Toast.makeText(this, "Invalid Username", Toast.LENGTH_SHORT).show()
                        }

                        password != user.password -> {
                            Toast.makeText(this, "Invalid Password", Toast.LENGTH_SHORT).show()
                        }

                        else -> {
                            Toast.makeText(this, "Successfully Logged In", Toast.LENGTH_SHORT).show()

                            val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)

                            prefs.edit()
                                .putString("logged_user", username)
                                .putString("role", user.role)
                                .apply()

                            if (user.role == "manager") {
                                startActivity(Intent(this, ManagerActivity::class.java))
                            } else {
                                startActivity(Intent(this, MainActivity::class.java))
                            }
                            finish()
                        }
                    }
                }
            }.start()
        }

        btnRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}
