package com.websarva.wings.android.sample

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_score_screen.*

class ScoreScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_score_screen)

        home.setOnClickListener {
            val intent_max = Intent(this@ScoreScreen, Home::class.java)
            startActivity(intent_max)
            finish()
        }

        reset.setOnClickListener {
            val intent_max = Intent(this@ScoreScreen, MainActivity::class.java)
            startActivity(intent_max)
            finish()
        }
        ans.setOnClickListener {
            val intent_max = Intent(this@ScoreScreen, MainMax::class.java)
            startActivity(intent_max)
            finish()
        }
    }
}