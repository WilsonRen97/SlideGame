package com.wilsontryingapp2023.slidegame


import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView


class SplashActivity : Activity() {
    private var singlePlayerGameBtn : ImageView? = null
    private var twoPlayerGameBtn : ImageView? = null
    private var questionBtn : ImageView? = null
    private var settingBtn : ImageView? = null



    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        setContentView(R.layout.splash_activity)
        singlePlayerGameBtn = findViewById(R.id.imageView1)
        twoPlayerGameBtn = findViewById(R.id.imageView2)
        questionBtn = findViewById(R.id.question_mark)
        settingBtn = findViewById(R.id.setting_gear)

        singlePlayerGameBtn!!.setOnClickListener {_ ->
            val i = Intent(this, MainActivity::class.java)
            i.putExtra("game_mode", GameMode.ONE_PLAYER)
            // MyView.mode = GameMode.ONE_PLAYER
            startActivity(i)
        }
        twoPlayerGameBtn!!.setOnClickListener{_ ->
            val i = Intent(this, MainActivity::class.java)
            i.putExtra("game_mode", GameMode.TWO_PLAYER)
            // MyView.mode = GameMode.TWO_PLAYER
            startActivity(i)
        }
        questionBtn!!.setOnClickListener {_ ->
            val ab: AlertDialog.Builder = AlertDialog.Builder(this)
            ab.setTitle(R.string.about)
            ab.setMessage(R.string.description)
            ab.setCancelable(false)
            ab.setPositiveButton(R.string.understand) { _, _ -> println("Done") }
            val box: AlertDialog = ab.create()
            box.show()
        }
        settingBtn!!.setOnClickListener {_ ->
            val i = Intent(this, Prefs::class.java)
            startActivity(i)
        }
    }
}