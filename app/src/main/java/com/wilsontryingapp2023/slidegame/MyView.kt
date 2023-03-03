package com.wilsontryingapp2023.slidegame

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.media.MediaPlayer
import android.os.Looper
import android.view.MotionEvent
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageView


class MyView(c: Context?) : AppCompatImageView(c!!), TickListener {
    private val p: Paint = Paint()
    private val p2: Paint = Paint()
    private var w = 0f
    private var h = 0f
    private var sideMargin = 0f
    private var verticalMargin = 0f
    private var gridLength = 0f
    private var mathDone: Boolean
    private var gridSize = 0
    private val buttons: Array<Btn?> = arrayOfNulls(10)
    private val tokens = ArrayList<Token>()
    private var timer: Timer? = null
    private var engine: GameBoard = GameBoard()
    private var currentPlayer: Player
    private var soundTrack: MediaPlayer? = null
    private var player1WinCount = 0
    private var player2WinCount = 0
    var mode : GameMode? = null

    init {
        p.color = Color.BLACK
        p2.color = Color.BLACK
        p2.textSize = 70f
        mathDone = false
        currentPlayer = Player.X
        setImageResource(R.drawable.back)
        scaleType = ScaleType.FIT_XY
        if (Prefs.soundOn(c)) {
            soundTrack = MediaPlayer.create(c, R.raw.ukulele)
            soundTrack?.start()
            soundTrack?.isLooping = true
        }
    }


    override fun onDraw(c: Canvas) {
        super.onDraw(c)
        if (!mathDone) {
            println("we are here....")
            mathDone = true
            w = width.toFloat()
            h = height.toFloat()
            p.strokeWidth = w * 0.015f
            sideMargin = w * 0.2f
            verticalMargin = (h - w + 2 * sideMargin) / 2
            gridLength = (w - 2 * sideMargin) / 5
            makeButtons()
            timer = Timer.factory(Prefs.getSpeed(context).toLong(), Looper.getMainLooper())
            timer!!.unPaused()
            timer!!.clearAll()
            timer!!.register(this)
        }
        val winnerState1: String = resources.getString(R.string.winnerX)
        val winnerState2: String = resources.getString(R.string.winnerY)
        c.drawText(winnerState1 + resources.getString(R.string.winCount) + player1WinCount, 50f, 100f, p2)
        c.drawText(winnerState2 + resources.getString(R.string.winCount) + player2WinCount, 50f, 250f, p2)

        // draw the grids, tokens and buttons
        drawGrid(c)
        for (token in tokens) {
            if (!token.isVisible(h)) {
                token.changeVelocity(0, 0)
                tokens.remove(token)
                timer!!.unregister(token)
                break
            }
        }
        for (token in tokens) {
            token.draw(c)
        }
        for (button in buttons) {
            button!!.drawBtn(c)
        }
        var check = true
        for (token in tokens) {
            if (token.velocity.x != 0f || token.velocity.y != 0f) {
                check = false
            }
        }
        if (check) {
            if (engine.checkForWin() !== Player.BLANK && engine.checkForWin() !== Player.TIE) {
                timer!!.setPaused()
                val ab: AlertDialog.Builder = AlertDialog.Builder(context)
                ab.setTitle(R.string.gameOver)
                val again: String =
                    engine.checkForWin().toString() + resources.getString(R.string.winner_sentence)
                ab.setMessage(again)
                updateWinner(engine.checkForWin())
                ab.setCancelable(false)
                ab.setPositiveButton(R.string.yes) { _, _ -> restartGame() }
                ab.setNegativeButton(R.string.no) {_, _ ->
                    (context as Activity).finish()
                }
                val box: AlertDialog = ab.create()
                box.show()
            } else {
                if (mode == GameMode.ONE_PLAYER && currentPlayer == Player.O) {
                    // pick a random button and submit
                    val num: Int = engine.aiMove()
                    if (num < 5) {
//                        char[] letters = {'A', 'B', 'C', 'D', 'E'};
//                        int randomNum = (int) Math.floor(Math.random() * 5);
//                        Btn button = buttons[randomNum];
                        val button: Btn? = buttons[num]
                        engine.submitMove(button!!.char)
                        val token = Token(
                            resources,
                            gridLength.toInt(),
                            button.boundsX(),
                            button.boundsY(),
                            ('A'.code - 1).toChar(),
                            button.char
                        )
                        tokens.add(token)
                        timer!!.register(token)
                        // move the neighbors
                        val neighbors: ArrayList<Token> = ArrayList()
                        neighbors.add(token)
                        moveVerticalNeighbors(button, neighbors)
                    } else {
                        val button: Btn? = buttons[num]
                        engine.submitMove(button!!.char)
                        val token = Token(
                            resources,
                            gridLength.toInt(),
                            button.boundsX(),
                            button.boundsY(),
                            button.char,
                            '0'
                        )
                        tokens.add(token)
                        timer!!.register(token)

                        // move the neighbors
                        val neighbors: ArrayList<Token> = ArrayList()
                        neighbors.add(token)
                        moveHorizontalNeighbors(button, neighbors)
                    }
                    changePlayer()
                }
            }
        }
    }

    private fun updateWinner(winner: Player) {
        if (winner == Player.X) {
            player1WinCount++
        } else if (winner == Player.O) {
            player2WinCount++
        }
    }

    private fun restartGame() {
        Token.setPlayer()
        timer!!.clearAll()
        tokens.clear()
        timer!!.unPaused()
        mathDone = false
        engine = GameBoard()
        currentPlayer = Player.X
        invalidate()
    }

    override fun onTouchEvent(m: MotionEvent): Boolean {
        if (Token.tokenMoving()) {
            for (button in buttons) {
                button!!.unpress()
            }
            return true
        }
        val x: Float = m.x
        val y: Float = m.y
        var btnTouched = false
        if (m.action == MotionEvent.ACTION_DOWN) {
            for (button in buttons) {
                if (button!!.contains(x, y)) {
                    engine.submitMove(button.char)
                    button.press()
                    btnTouched = true
                    changePlayer()
                    // create a token, first check if it's a row button
                    if (button.isRowBtn()) {
                        val token = Token(
                            resources,
                            gridLength.toInt(),
                            button.boundsX(),
                            button.boundsY(),
                            ('A'.code - 1).toChar(),
                            button.char
                        )
                        tokens.add(token)
                        timer!!.register(token)

                        // move the neighbors
                        val neighbors: ArrayList<Token> = ArrayList()
                        neighbors.add(token)
                        moveVerticalNeighbors(button, neighbors)
                    } else {
                        val token = Token(
                            resources,
                            gridLength.toInt(),
                            button.boundsX(),
                            button.boundsY(),
                            button.char,
                            '0'
                        )
                        tokens.add(token)
                        timer!!.register(token)

                        // move the neighbors
                        val neighbors: ArrayList<Token> = ArrayList()
                        neighbors.add(token)
                        moveHorizontalNeighbors(button, neighbors)
                    }
                }
            }
            if (!btnTouched) {
                val t: Toast = Toast.makeText(context, resources.getString(R.string.button_notification), Toast.LENGTH_SHORT)
                t.show()
            }
        }
        if (m.action == MotionEvent.ACTION_UP) {
            for (button in buttons) {
                button!!.unpress()
            }
        }
        invalidate()
        return true
    }

    /**
     * move all the vertical neighbors
     * @param button
     * @param neighborList
     */
    private fun moveVerticalNeighbors(button: Btn?, neighborList: ArrayList<Token>) {
        val rowLetters = charArrayOf('A', 'B', 'C', 'D', 'E')
        for (i in rowLetters.indices) {
            val dog: Token? = findDog(rowLetters[i], button!!.char)
            if (dog != null) {
                neighborList.add(dog)
            } else {
                break
            }
        }
        for (token in neighborList) {
            token.setYDestination(gridSize)
            token.changeVelocity(0, 20)
            token.row = (token.row + 1) as Char
        }
    }

    /**
     * move all the horizontal neighbors
     * @param button
     * @param neighborList
     */
    private fun moveHorizontalNeighbors(button: Btn?, neighborList: ArrayList<Token>) {
        val columnLetters = charArrayOf('1', '2', '3', '4', '5')
        for (i in columnLetters.indices) {
            val dog: Token? = findDog(button!!.char, columnLetters[i])
            if (dog != null) {
                neighborList.add(dog)
            } else {
                break
            }
        }
        for (token in neighborList) {
            token.setXDestination(gridSize)
            token.changeVelocity(20, 0)
            //System.out.println(token.getColumn());
            token.column = ((token.column + 1) as Char)
        }
    }

    /**
     * draw the grid
     * @param c
     */
    private fun drawGrid(c: Canvas) {
        c.drawLine(sideMargin, verticalMargin, w - sideMargin, verticalMargin, p)
        c.drawLine(
            sideMargin,
            verticalMargin + gridLength,
            w - sideMargin,
            verticalMargin + gridLength,
            p
        )
        c.drawLine(
            sideMargin,
            verticalMargin + gridLength * 2,
            w - sideMargin,
            verticalMargin + gridLength * 2,
            p
        )
        c.drawLine(
            sideMargin,
            verticalMargin + gridLength * 3,
            w - sideMargin,
            verticalMargin + gridLength * 3,
            p
        )
        c.drawLine(
            sideMargin,
            verticalMargin + gridLength * 4,
            w - sideMargin,
            verticalMargin + gridLength * 4,
            p
        )
        c.drawLine(
            sideMargin,
            verticalMargin + gridLength * 5,
            w - sideMargin,
            verticalMargin + gridLength * 5,
            p
        )
        // vertical lines
        c.drawLine(sideMargin, verticalMargin, sideMargin, verticalMargin + gridLength * 5, p)
        c.drawLine(
            sideMargin + gridLength,
            verticalMargin,
            sideMargin + gridLength,
            verticalMargin + gridLength * 5,
            p
        )
        c.drawLine(
            sideMargin + gridLength * 2,
            verticalMargin,
            sideMargin + gridLength * 2,
            verticalMargin + gridLength * 5,
            p
        )
        c.drawLine(
            sideMargin + gridLength * 3,
            verticalMargin,
            sideMargin + gridLength * 3,
            verticalMargin + gridLength * 5,
            p
        )
        c.drawLine(
            sideMargin + gridLength * 4,
            verticalMargin,
            sideMargin + gridLength * 4,
            verticalMargin + gridLength * 5,
            p
        )
        c.drawLine(
            sideMargin + gridLength * 5,
            verticalMargin,
            sideMargin + gridLength * 5,
            verticalMargin + gridLength * 5,
            p
        )
    }

    /**
     * instantiate all the buttons
     */
    private fun makeButtons() {
        gridSize = gridLength.toInt()
        val gridXLeft = sideMargin
        val gridXLeft2 = sideMargin - gridSize
        val gridYTop = verticalMargin - gridSize
        val gridYTop2 = verticalMargin
        buttons[0] = Btn(resources, '1', gridSize, gridXLeft, gridYTop)
        buttons[1] = Btn(resources, '2', gridSize, gridXLeft + gridSize, gridYTop)
        buttons[2] = Btn(resources, '3', gridSize, gridXLeft + gridSize * 2, gridYTop)
        buttons[3] = Btn(resources, '4', gridSize, gridXLeft + gridSize * 3, gridYTop)
        buttons[4] = Btn(resources, '5', gridSize, gridXLeft + gridSize * 4, gridYTop)
        buttons[5] = Btn(resources, 'A', gridSize, gridXLeft2, gridYTop2)
        buttons[6] = Btn(resources, 'B', gridSize, gridXLeft2, gridYTop2 + gridSize)
        buttons[7] = Btn(resources, 'C', gridSize, gridXLeft2, gridYTop2 + gridSize * 2)
        buttons[8] = Btn(resources, 'D', gridSize, gridXLeft2, gridYTop2 + gridSize * 3)
        buttons[9] = Btn(resources, 'E', gridSize, gridXLeft2, gridYTop2 + gridSize * 4)
    }

    override fun tick() {
        invalidate()
    }

    /**
     * find a dog
     * @param row
     * @param column
     * @return
     */
    private fun findDog(row: Char, column: Char): Token? {
        for (token in tokens) {
            if (token.row == row && token.column == column) {
                return token
            }
        }
        return null
    }

    /**
     * swap player
     */
    private fun changePlayer() {
        currentPlayer = if (currentPlayer == Player.X) {
            Player.O
        } else {
            Player.X
        }
    }

    /**
     * pause music
     */
    private fun pauseMusic() {
        soundTrack?.pause()
    }

    private fun restartMusic() {
        soundTrack?.start()
    }

    /**
     * call pause music method
     */
    fun gotBackground() {
        pauseMusic()
    }

    fun gotForeground() {
        restartMusic()
    }

    /**
     * release the soundtrack
     */
    fun cleanupBeforeShutDown() {
        soundTrack?.release()
    }
}
