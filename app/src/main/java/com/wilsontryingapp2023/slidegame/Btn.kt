package com.wilsontryingapp2023.slidegame

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.RectF


class Btn(res: Resources?, label: Char, size: Int, x: Float, y: Float) {
    private var unpressedImg: Bitmap
    private var pressedImg: Bitmap

    /**
     *
     * @return the label of the button
     */
    val char: Char
    private var pressed: Boolean
    private val bounds: RectF

    init {
        unpressedImg = BitmapFactory.decodeResource(res, R.drawable.unpressed_button, null)
        unpressedImg = Bitmap.createScaledBitmap(unpressedImg, size, size, true)
        pressedImg = BitmapFactory.decodeResource(res, R.drawable.pressed_button, null)
        pressedImg = Bitmap.createScaledBitmap(pressedImg, size, size, true)
        char = label
        bounds = RectF(x, y, x + size, y + size)
        pressed = false
    }

    /**
     * Draw the button according to if the button is pressed or not
     * @param c the Canvas class
     */
    fun drawBtn(c: Canvas) {
        if (pressed) {
            c.drawBitmap(pressedImg, bounds.left, bounds.top, null)
        } else {
            c.drawBitmap(unpressedImg, bounds.left, bounds.top, null)
        }
    }

    /**
     * used to check if the click event is on one of the buttons
     * @param x position of x
     * @param y position of y
     * @return a boolean if the button contains (x, y)
     */
    fun contains(x: Float, y: Float): Boolean {
        return bounds.contains(x, y)
    }

    /**
     * setter for button's pressed
     */
    fun press() {
        pressed = true
    }

    fun unpress() {
        pressed = false
    }

    /**
     *
     * @return the position of button's x position
     */
    fun boundsX(): Float {
        return bounds.left
    }

    fun boundsY(): Float {
        return bounds.top
    }

    /**
     *
     * @return if a button is a row button
     */
    fun isRowBtn(): Boolean {
        return char == '1' || char == '2' || char == '3' || char == '4' || char == '5'
    }
}
