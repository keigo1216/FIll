package com.websarva.wings.android.sample

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View

class Home : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
    }

    fun start_bt(view: View){
        val intent = Intent(this@Home, MainActivity::class.java)
        startActivity(intent)
    }
}