package com.wilsontryingapp2023.slidegame

import android.content.res.Resources
import android.graphics.*


class Token(res: Resources?, size: Int, x: Float, y: Float, row: Char, column: Char) : TickListener {
    private val bounds: RectF
    private var dog1: Bitmap
    private var dog2: Bitmap
    private var dog: Bitmap? = null
    val velocity: PointF
    var row: Char
    var column: Char
    private val destination: PointF
    private var falling: Boolean

    init {
        dog1 = BitmapFactory.decodeResource(res, R.drawable.doggy3)
        dog1 = Bitmap.createScaledBitmap(dog1, size, size, true)
        dog2 = BitmapFactory.decodeResource(res, R.drawable.doggy4)
        dog2 = Bitmap.createScaledBitmap(dog2, size, size, true)
        bounds = RectF(x, y, size.toFloat(), size.toFloat())
        velocity = PointF(1f, 1f)
        this.row = row
        this.column = column
        destination = PointF(x, y)
        falling = false

        // choose a dog
        dog = if (player % 2 == 0) {
            dog1
        } else {
            dog2
        }
        player++
    }

    /**
     * change the bounds of the token by its velocity
     */
    private fun move() {
        if (falling) {
            velocity.y *= 2f
        } else if (destination.y - bounds.top <= 15 && velocity.y != 0f) {
            changeVelocity(0, 0)
            if (row > 'E') {
                changeVelocity(0, 1)
                falling = true
            }
        } else if (destination.x - bounds.left <= 15 && velocity.x != 0f) {
            changeVelocity(0, 0)
            if (column > '5') {
                changeVelocity(0, 1)
                falling = true
            }
        }
        bounds.offset(velocity.x, velocity.y)
    }

    /**
     * draw a token
     * @param c
     */
    fun draw(c: Canvas) {
        c.drawBitmap(dog!!, bounds.left, bounds.top, null)
    }

    fun changeVelocity(x: Int, y: Int) {
        if (x != 0 || y != 0) {
            movers++
        } else {
            movers--
        }
        velocity.x = x.toFloat()
        velocity.y = y.toFloat()
        //System.out.println(movers);
    }

    override fun tick() {
        move()
    }

    fun setXDestination(x: Int) {
        destination.x += x.toFloat()
    }

    fun setYDestination(y: Int) {
        destination.y += y.toFloat()
    }

    /**
     * check visibility
     * @param windowHeight
     * @return
     */
    fun isVisible(windowHeight: Float): Boolean {
        return bounds.top <= windowHeight
    }

    companion object {
        private var player = 0
        private var movers = 0
        fun setPlayer() {
            player = 0
        }

        /**
         * check if some tokens are still moving
         * @return
         */
        fun tokenMoving(): Boolean {
            return movers > 0
        }
    }
}