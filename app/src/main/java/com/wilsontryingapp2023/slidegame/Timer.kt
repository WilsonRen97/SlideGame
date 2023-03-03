package com.wilsontryingapp2023.slidegame

import android.os.Handler
import android.os.Looper
import android.os.Message


class Timer(d: Long, looper: Looper) : Handler(looper) {
    private val listeners: MutableList<TickListener>
    private var paused: Boolean
    private val delay: Long

    init {
        listeners = ArrayList()
        sendMessageDelayed(obtainMessage(), 0)
        paused = false
        delay = d
    }

    override fun handleMessage(msg: Message) {
        if (!paused) {
            notifyTickListeners()
        }
        sendMessageDelayed(obtainMessage(), delay)
    }

    fun register(t: TickListener) {
        listeners.add(t)
    }

    fun unregister(t: TickListener) {
        listeners.remove(t)
    }

    fun clearAll() {
        listeners.clear()
    }

    private fun notifyTickListeners() {
        println(listeners.size)
        for (t in listeners) {
            t.tick()
        }
    }

    fun setPaused() {
        paused = true
    }

    fun unPaused() {
        paused = false
    }


    companion object {
        private var singleton: Timer? = null
        fun factory(d: Long, looper : Looper): Timer? {
            if (singleton == null || singleton!!.delay != d) {
                singleton = Timer(d, looper)
            }
            return singleton
        }
    }
}