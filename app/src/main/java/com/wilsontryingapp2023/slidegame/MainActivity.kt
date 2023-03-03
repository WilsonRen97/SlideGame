package com.wilsontryingapp2023.slidegame

import android.app.Activity
import android.content.Intent
import android.os.Bundle

class MainActivity : Activity() {
    private var mv: MyView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var myIntent : Intent = intent
        var gameMode : GameMode = myIntent!!.getSerializableExtra("game_mode") as GameMode
        mv = MyView(this)
        mv!!.mode = gameMode
        setContentView(mv)
    }

    public override fun onPause() {
        super.onPause()
        mv!!.gotBackground()
    }

    public override fun onResume() {
        super.onResume()
        mv!!.gotForeground()
    }

    public override fun onDestroy() {
        super.onDestroy()
        mv!!.cleanupBeforeShutDown()
    }
}