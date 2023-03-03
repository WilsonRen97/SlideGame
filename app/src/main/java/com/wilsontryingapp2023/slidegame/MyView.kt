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
    private var gridSize = 0
    private var mathDone: Boolean = false
    private val buttons: Array<Btn?> = arrayOfNulls(10)

    private var ab: AlertDialog.Builder = AlertDialog.Builder(context)
    private var tokens: ArrayList<Token> = ArrayList()
    private var timer: Timer? = null
    private var engine: GameBoard = GameBoard()
    private var currentPlayer: Player = Player.X
    private var soundTrack: MediaPlayer? = null
    private var player1WinCount = 0
    private var player2WinCount = 0
    var mode: GameMode? = null

    init {
        // 遊戲初始的設定
        p.color = Color.BLACK
        p2.color = Color.BLACK
        p2.textSize = 40f
        setImageResource(R.drawable.back2)
        scaleType = ScaleType.FIT_XY

        if (SettingsActivity.soundOn(c)) {
            soundTrack = MediaPlayer.create(c, R.raw.ukulele)
            soundTrack?.start()
            soundTrack?.isLooping = true
        }
    }


    override fun onDraw(c: Canvas) {
        super.onDraw(c)
        if (!mathDone) {
            // 先設定mathDone已經完成
            // 取得螢幕的寬度與高度，並根據螢幕寬高來設定p的strokeWidth, sideMargin, verticalMargin, gridLength等等
            // 再來根據以上數值，執行makeButtons()
            mathDone = true
            w = width.toFloat()
            h = height.toFloat()
            p.strokeWidth = w * 0.015f
            sideMargin = w * 0.2f
            verticalMargin = (h - w + 2 * sideMargin) / 2
            gridLength = (w - 2 * sideMargin) / 5
            makeButtons()

            // 從factory拿到timer，並且register MyView物件
            timer = Timer.factory(SettingsActivity.getSpeed(context).toLong(), Looper.getMainLooper())
            // 我們會執行mathDone內部的程式碼，只有幾種可能：(1)第一次開啟App以及遊戲,
            // (2)遊戲玩到一半，透過返回鍵回到SplashActivity, (3)選擇No不繼續玩遊戲，執行了finish()
            // 在第三種情況時，我們的Timer是被固定住的。所以每當mathDone是false時，代表我們遇到以上三種情況之一，
            // 我們用timer!!.unPaused()可以確保Timer可以繼續運作，從被固定的情況下回到運作情況
            timer!!.unPaused()
            timer!!.register(this)
        }

        // 每次畫圖時，更新玩家勝負資訊
        val winnerState1: String = resources.getString(R.string.winnerX)
        val winnerState2: String = resources.getString(R.string.winnerY)
        c.drawText(
            winnerState1 + " " + resources.getString(R.string.winCount) + player1WinCount,
            50f,
            100f,
            p2
        )
        c.drawText(
            winnerState2 + " " + resources.getString(R.string.winCount) + player2WinCount,
            50f,
            150f,
            p2
        )

        // 畫出Grid
        drawGrid(c)
        // 檢查有沒有跳出螢幕的token
        for (token in tokens) {
            if (!token.isVisible(h)) {
                token.changeVelocity(0, 0)
                tokens.remove(token)
                timer!!.unregister(token)
                break
            }
        }
        // 畫出所有的tokens
        for (token in tokens) {
            token.draw(c)
        }
        // 畫出所有的buttons
        for (button in buttons) {
            button!!.drawBtn(c)
        }

        var checkWeCanProceed = true
        for (token in tokens) {
            // 等到所有的token的速度都歸零後，才能確認遊戲有無贏家
            if (token.velocity.x != 0f || token.velocity.y != 0f) {
                checkWeCanProceed = false
            }
        }
        // 確認有無贏家，以及讓AI做出下個動作
        if (checkWeCanProceed) {
            // 如果遊戲有贏家了
            if (engine.checkForWin() !== Player.BLANK) {
                timer!!.setPaused()
                ab.setTitle(R.string.gameOver)
                var again: String? = null
                var winnerString: String? = null
                if (engine.checkForWin() !== Player.TIE) {
                    winnerString = if (engine.checkForWin() == Player.X) {
                        resources.getString(R.string.winnerX)
                    } else {
                        resources.getString(R.string.winnerY)
                    }
                    again = winnerString + " " + resources.getString(R.string.winner_sentence)
                } else {
                    again = resources.getString(R.string.tie_sentence)
                }

                ab.setMessage(again)
                updateWinner(engine.checkForWin())
                ab.setCancelable(false)
                ab.setPositiveButton(R.string.yes) { _, _ -> restartGame() }
                ab.setNegativeButton(R.string.no) { _, _ ->
                    (context as Activity).finish() // 會去執行MainActivity的onDestroy()，onDestroy()會去執行MyView的cleanupBeforeShutDown()
                }
                val box: AlertDialog = ab.create()
                box.show()
            } else {
                // 如果遊戲還沒有贏家，且目前是跟AI玩的話，且目前輪到AI下的話，我們可以讓AI做出move
                if (mode == GameMode.ONE_PLAYER && currentPlayer == Player.O) {
                    val num: Int = engine.aiMove()
                    val button: Btn? = buttons[num]
                    engine.submitMove(button!!.char)

                    // 依據數字來判斷，AI選定的是哪個button
                    // 0, 1, 2, 3, 4都是橫向的buttons, 5, 6, 7, 8, 9都是直向的buttons
                    val token: Token = if (num < 5) {
                        Token(
                            resources,
                            gridLength.toInt(),
                            button.boundsX(),
                            button.boundsY(),
                            ('A'.code - 1).toChar(),
                            button.char
                        )
                    } else {
                        Token(
                            resources,
                            gridLength.toInt(),
                            button.boundsX(),
                            button.boundsY(),
                            button.char,
                            '0'
                        )
                    }
                    tokens.add(token)
                    timer!!.register(token)
                    // move the neighbors
                    val neighbors: ArrayList<Token> = ArrayList()
                    neighbors.add(token)
                    if (num < 5) {
                        moveVerticalNeighbors(button, neighbors)
                    } else {
                        moveHorizontalNeighbors(button, neighbors)
                    }

                    // 改變目前的Player
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
        // 遊戲結束時，在同個MyView物件中，需要被重新設定的部分
        Token.resetPlayer()
        timer!!.clearAll()
        timer!!.unPaused()

        tokens.clear()
        mathDone = false
        engine = GameBoard()
        currentPlayer = Player.X
        invalidate()

        // 遊戲結束時，若有新的MyView物件的話，需要被重新設定的部分：
        // Token.resetPlayer()
        // timer!!.clearAll()
        // timer!!.unPaused()

        // tokens.clear()的部分，在新的MyView物件中會重新設定新的tokens arraylist，所以不用管
        // math false的部分，在新的MyView物件中會重新設定
        // engine = GameBoard()的部分，在新的MyView物件中會重新設定
        // currentPlayer = Player.X的部分，在新的MyView物件中會重新設定
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
                    val token: Token = if (button.isRowBtn()) {
                        Token(
                            resources,
                            gridLength.toInt(),
                            button.boundsX(),
                            button.boundsY(),
                            ('A'.code - 1).toChar(),
                            button.char
                        )
                    } else {
                        Token(
                            resources,
                            gridLength.toInt(),
                            button.boundsX(),
                            button.boundsY(),
                            button.char,
                            '0'
                        )
                    }
                    tokens.add(token)
                    timer!!.register(token)
                    // move the neighbors
                    val neighbors: ArrayList<Token> = ArrayList()
                    neighbors.add(token)
                    if (button.isRowBtn()) {
                        moveVerticalNeighbors(button, neighbors)
                    } else {
                        moveHorizontalNeighbors(button, neighbors)
                    }
                }
            }
            if (!btnTouched) {
                val t: Toast = Toast.makeText(
                    context,
                    resources.getString(R.string.button_notification),
                    Toast.LENGTH_SHORT
                )
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
    private fun moveVerticalNeighbors(button: Btn, neighborList: ArrayList<Token>) {
        val rowLetters = charArrayOf('A', 'B', 'C', 'D', 'E')
        for (i in rowLetters.indices) {
            val dog: Token? = findDog(rowLetters[i], button.char)
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
        // 0, 1, 2, 3, 4都是橫向的buttons
        buttons[0] = Btn(resources, '1', gridSize, gridXLeft, gridYTop)
        buttons[1] = Btn(resources, '2', gridSize, gridXLeft + gridSize, gridYTop)
        buttons[2] = Btn(resources, '3', gridSize, gridXLeft + gridSize * 2, gridYTop)
        buttons[3] = Btn(resources, '4', gridSize, gridXLeft + gridSize * 3, gridYTop)
        buttons[4] = Btn(resources, '5', gridSize, gridXLeft + gridSize * 4, gridYTop)
        // 5, 6, 7, 8, 9都是直向的buttons
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
     * call pause music method
     */
    fun gotBackground() {
        soundTrack?.pause()
    }

    fun gotForeground() {
        soundTrack?.start()
    }

    /**
     * release the soundtrack
     */
    fun cleanupBeforeShutDown() {
        Token.resetPlayer()
        timer!!.clearAll()
        soundTrack?.release() // 釋放MediaPlayer使用的音樂檔案
    }
}
