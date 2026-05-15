package com.example.nammamela

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("DEBUG", "SplashActivity Started")
        setContentView(R.layout.activity_splash)

        val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val loggedUser = prefs.getString("logged_user", null)
        val loggedRole = prefs.getString("logged_role", null)

        Handler(Looper.getMainLooper()).postDelayed({
            val intent = if (loggedUser != null) {
                if (loggedRole == "manager") {
                    Intent(this, ManagerActivity::class.java)
                } else {
                    Intent(this, MainActivity::class.java)
                }
            } else {
                Intent(this, LoginActivity::class.java)
            }
            startActivity(intent)
            finish()
        }, 1500)
    }
}
