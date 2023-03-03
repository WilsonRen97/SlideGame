package com.wilsontryingapp2023.slidegame

import kotlinx.coroutines.withTimeoutOrNull
import kotlin.math.floor

class GameBoard {
    /**
     * grid is a two-dimensional (5x5) array representing the actual game-board
     */
    private val grid: Array<Array<Player?>>

    /**
     * DIM is set to 5. Change this if you want a bigger/smaller game-board.
     * The "true" Slide game uses a 5x5 board.
     */
    private val DIM = 5

    /**
     * keeps track of whose turn it is (either Player.X or Player.O)
     */
    private var currentPlayer: Player

    /**
     * Constructor for our GameBoard class. Initializes the game-board to blank.
     * Arbitrarily sets X as the first player. I guess you could change it to O
     * if you want. Or better yet, make it random.
     */
    init {
        grid = Array(DIM) { arrayOfNulls(DIM) }
        clear()
        currentPlayer = Player.X
    }

    /**
     * Just "blanks out" the game-board.
     */
    private fun clear() {
        for (i in 0 until DIM) {
            for (j in 0 until DIM) {
                grid[i][j] = Player.BLANK
            }
        }
    }

    /**
     * This method processes a single move for the current player. The input parameter is the row or column the user selected.
     * Tokens are "inserted" into the top of the grid (for vertical moves) or into the left side (for horizontal moves).
     * Tokens move from top to bottom (for vertical moves) or from left to right (for horizontal moves).
     * Existing tokens slide down (or right) to make way for the new tokens. If a column is full, the bottommost token
     * is removed. If a row is full, the rightmost token is removed.
     * At the conclusion of a move, the currentPlayer variable is toggled (X to O, or O to X).
     * @param move one of the characters '1', '2', '3', '4', '5' (for vertical moves) or 'A', 'B', 'C', 'D', 'E' (for horizontal moves). Any other values will result in buggy results.
     */
    fun submitMove(move: Char) {
        if (move in '1'..'5') {
            //vertical move, move stuff down
            val col = ("" + move).toInt() - 1
            var newVal: Player? = currentPlayer
            for (i in 0 until DIM) {
                if (grid[i][col] === Player.BLANK) {
                    grid[i][col] = newVal
                    break
                } else {
                    val tmp: Player? = grid[i][col]
                    grid[i][col] = newVal
                    newVal = tmp
                }
            }
        } else { //A-E
            //horizontal move, move stuff right
            val row = (move.code - 'A'.code)
            var newVal: Player? = currentPlayer
            for (i in 0 until DIM) {
                if (grid[row][i] === Player.BLANK) {
                    grid[row][i] = newVal
                    break
                } else {
                    val tmp: Player? = grid[row][i]
                    grid[row][i] = newVal
                    newVal = tmp
                }
            }
        }
        currentPlayer = if (currentPlayer === Player.X) {
            Player.O
        } else {
            Player.X
        }
    }

    /**
     * check if there's three dogs in a row
     * @return integer as button position
     */
    fun aiMove(): Int {
        var threeInOne: Boolean
        //check all rows
        for (i in 0 until DIM) {
            for (j in 0..2) {
                threeInOne = true
                val first: Player? = grid[i][j]
                for (k in j until j + 3) {
                    if (grid[i][k] !== first) {
                        threeInOne = false
                        break
                    }
                }
                if (threeInOne && first === Player.X) {
                    println("found $i and $j")
                    return i + 5
                }
            }
        }

        //check all columns
        for (i in 0 until DIM) {
            for (j in 0..2) {
                threeInOne = true
                val first: Player? = grid[j][i]
                for (k in j until j + 3) {
                    if (grid[k][i] !== first) {
                        threeInOne = false
                    }
                }
                if (threeInOne && first === Player.X) {
                    println("found $i and $j")
                    return i
                }
            }
        }
        return floor(Math.random() * 10).toInt()
    }

    /**
     * Checks all rows, columns and the two diagonals for five matching tokens in a row.
     * I'll explain the logic for rows. The logic for columns and diagonals are analogous.
     * For each of the five rows, check the value of the leftmost element. If it's not blank,
     * loop through the remaining four elements to see if they match the first one. If
     * they do, stop and declare that player the winner. But if any does not match the
     * first one, skip that row and search the next row for matches in the same manner.
     * @return the value of the winning player, X or O or TIE. Returns BLANK if no one has yet won (the most common state).
     */
    fun checkForWin(): Player {
        var winner: Player = Player.BLANK
        var winners: ArrayList<Player> = ArrayList()

        //check all rows
        for (i in 0 until DIM) {
            if (grid[i][0] !== Player.BLANK) {
                val firstElement = grid[i][0]!!
                // 確認是否有跟第一個element不同的值
                var allSame : Boolean = true
                for (j in 0 until DIM) {
                    if (grid[i][j] != firstElement) {
                        allSame = false
                        break
                    }
                }
                if (allSame) {
                    winners.add(firstElement)
                }
            }
        }
        if (winners.size == 1) {
            return winners[0]
        } else if (winners.size > 1) {
            return Player.TIE
        } else {
            // row的部分沒有發現贏家，所以winners arraylist還是空的，
            // 可以繼續往下走
        }

        //check all columns
        for (i in 0 until DIM) {
            if (grid[0][i] !== Player.BLANK) {
                val firstElement = grid[0][i]!!
                var allSame : Boolean = true
                for (j in 0 until DIM) {
                    if (grid[j][i] !== firstElement) {
                        allSame = false
                        break
                    }
                }
                if (allSame) {
                    winners.add(firstElement)
                }
            }
        }

        if (winners.size == 1) {
            return winners[0]
        } else if (winners.size > 1) {
            return Player.TIE
        } else {
            // columns的部分沒有發現贏家，所以winners arraylist還是空的，
            // 可以繼續往下走
        }


        //check top-left -> bottom-right diagonal
        if (grid[0][0] !== Player.BLANK) {
            val firstElement = grid[0][0]!!
            var allSame : Boolean = true
            for (i in 0 until DIM) {
                if (grid[i][i] != firstElement) {
                    allSame = false
                    break
                }
            }
            if (allSame) {
                return firstElement
            }
        }

        //check bottom-left -> top-right diagonal
        if (grid[DIM - 1][0] !== Player.BLANK) {
            val firstElement = grid[DIM - 1][0]!!
            var allSame : Boolean = true
            for (i in 0 until DIM) {
                if (grid[DIM - 1 - i][i] !== firstElement) {
                    allSame = false
                    break
                }
            }
            if (allSame) {
                return firstElement
            }
        }

        return Player.BLANK
    }
}
