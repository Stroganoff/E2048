package com.example.e2048

import android.content.res.Resources
import android.graphics.Bitmap.createScaledBitmap
import android.graphics.BitmapFactory.decodeResource
import android.graphics.Canvas
import com.example.e2048.sprites.Sprite
import com.example.e2048.sprites.Tile
import kotlin.random.Random

class TileManager(
    private val resources: Resources,
    private val screenWidth: Int,
    private val screenHeight: Int,
    private val standardSize: Int,
    private val gridRows: Int,
    private val gridColumns: Int,
    private val initialNumberOfTiles: Int,
    private val gameManagerCallback: GameManagerCallback
) : TileManagerCallback, Sprite {

    private val tileBitmaps =
        arrayOf(
            R.drawable.one, R.drawable.two, R.drawable.three, R.drawable.four, R.drawable.five, R.drawable.six, R.drawable.seven,
            R.drawable.eight, R.drawable.nine, R.drawable.ten, R.drawable.eleven, R.drawable.twelve, R.drawable.thirteen,
            R.drawable.fourteen, R.drawable.fifteen, R.drawable.sixteen
        ).map {
            val bitmap = decodeResource(resources, it)
            createScaledBitmap(bitmap, standardSize, standardSize, false)
        }

    private lateinit var tileMatrix: Array<Array<Tile?>>
    private lateinit var movingTiles: List<Tile>
    private var isMoving = false
    private var needsToSpawn = false
    private var endgame = false

    init {
        initGame()
    }

    fun initGame() {
        endgame = false
        tileMatrix = Array(gridRows) { arrayOfNulls(gridColumns) }
        movingTiles = emptyList()

        /*var tilesAdded = 0
        while (tilesAdded < initialNumberOfTiles) {
            val x = Random.Default.nextInt(4)
            val y = Random.Default.nextInt(4)
            if (tileMatrix[x][y] == null) {
                tileMatrix[x][y] = Tile(
                    screenWidth = screenWidth,
                    screenHeight = screenHeight,
                    standardSize = standardSize,
                    matrixX = x,
                    matrixY = y,
                    callback = this
                )
                tilesAdded += 1
            }
        }*/

        for (x in 0..<4) {
            for (y in 0..<4) {
                if (x != 3 || y != 3) {
                    val tile = Tile(
                        screenWidth = screenWidth,
                        screenHeight = screenHeight,
                        standardSize = standardSize,
                        matrixX = x,
                        matrixY = y,
                        callback = this
                    )
                    tile.setLevel(3 * x + y + 4)
                    tileMatrix[x][y] = tile
                }
            }
        }
    }

    override fun draw(canvas: Canvas) {
        tileMatrix.forEachNonNull { tile -> tile.draw(canvas) }
        if (endgame) {
            gameManagerCallback.gameOver()
        }
    }

    override fun update() {
        tileMatrix.forEachNonNull { tile -> tile.update() }
    }

    override fun getBitmap(level: Int) = tileBitmaps[level - 1]

    override fun finishedMoving(tile: Tile) {
        movingTiles = movingTiles.filter { it == tile }
        if (movingTiles.isEmpty()) {
            isMoving = false
            spawn()
            checkEndgame()
        }
    }

    override fun updateScore(newPoints: Int) {
        gameManagerCallback.updateScore(newPoints)
    }

    override fun reached2048() {
        gameManagerCallback.reached2048()
    }

    private fun checkEndgame() {
        endgame = true

        for (x in 0..<gridColumns) {
            for (y in 0..<gridRows) {
                if (tileMatrix[x][y] == null) {
                    endgame = false
                }
            }
        }

        if (endgame) {
            for (x in 0..<gridColumns) {
                for (y in 0..<gridRows) {
                    if (
                        (x > 0 && tileMatrix[x - 1][y]?.getLevel() == tileMatrix[x][y]?.getLevel()) ||
                        (x < gridRows - 1 && tileMatrix[x][y]?.getLevel() == tileMatrix[x + 1][y]?.getLevel()) ||
                        (y > 0 && tileMatrix[x][y - 1]?.getLevel() == tileMatrix[x][y]?.getLevel()) ||
                        (y < gridColumns - 1 && tileMatrix[x][y]?.getLevel() == tileMatrix[x][y + 1]?.getLevel())
                    ) {
                        endgame = false
                    }
                }
            }
        }
    }

    private fun spawn() {
        if (needsToSpawn) {
            needsToSpawn = false
            var tileAdded = false
            while (!tileAdded) {
                val x = Random.Default.nextInt(4)
                val y = Random.Default.nextInt(4)

                if (tileMatrix[x][y] == null) {
                    tileMatrix[x][y] = Tile(
                        screenWidth = screenWidth,
                        screenHeight = screenHeight,
                        standardSize = standardSize,
                        matrixX = x,
                        matrixY = y,
                        callback = this
                    )
                    tileAdded = true
                }
            }
        }
    }

    private fun calculateSpawn(tileMatrix: Array<Array<Tile?>>, newMatrix: Array<Array<Tile?>>) {
        for (x in 0..<gridRows) {
            for (y in 0..<gridColumns) {
                if (newMatrix[x][y] != tileMatrix[x][y]) {
                    needsToSpawn = true
                    break
                }
            }
        }
    }

    fun onSwipe(direction: SwipeCallback.Direction) {
        if (!isMoving) {
            isMoving = true

            val newMatrix = when (direction) {
                SwipeCallback.Direction.UP -> moveUp()
                SwipeCallback.Direction.DOWN -> moveDown()
                SwipeCallback.Direction.LEFT -> moveLeft()
                SwipeCallback.Direction.RIGHT -> moveRight()

            }
            calculateSpawn(tileMatrix, newMatrix)
            tileMatrix = newMatrix
        }
    }

    private fun moveUp(): Array<Array<Tile?>> {
        val newMatrix: Array<Array<Tile?>> = Array(gridRows) { arrayOfNulls(gridColumns) }
        traverseMatrixForUp(tileMatrix) { x, y, currentTile ->
            // If tile present in matrix, define it in new.
            newMatrix[x][y] = currentTile
            for (k in x - 1 downTo 0) {
                // If upper element is not defined, move current one step to up position.
                val upperTile = newMatrix[k][y]
                if (upperTile == null) {
                    newMatrix[k][y] = currentTile
                    if (newMatrix[k + 1][y] == currentTile) {
                        newMatrix[k + 1][y] = null
                    }
                } else if (upperTile.getLevel() == currentTile.getLevel() && !upperTile.isLevelingUp()) {
                    // If upper element is defined, check if same level and combine it, else, do nothing.
                    newMatrix[k][y] = currentTile.levelUp()
                    if (newMatrix[k + 1][y] == currentTile) {
                        newMatrix[k + 1][y] = null
                    }
                } else {
                    break
                }
            }
        }

        traverseMatrixForUp(tileMatrix) { _, _, currentTile ->
            movePendingTile(currentTile, newMatrix)
        }
        return newMatrix
    }

    private fun moveDown(): Array<Array<Tile?>> {
        val newMatrix: Array<Array<Tile?>> = Array(gridRows) { arrayOfNulls(gridColumns) }
        traverseMatrixForDown(tileMatrix) { x, y, currentTile ->
            // If tile present in matrix, define it in new.
            newMatrix[x][y] = currentTile
            for (k in x + 1..<gridRows) {
                // If upper element is not defined, move current one step to down position.
                val lowerTile = newMatrix[k][y]
                if (lowerTile == null) {
                    newMatrix[k][y] = currentTile
                    if (newMatrix[k - 1][y] == currentTile) {
                        newMatrix[k - 1][y] = null
                    }
                } else if (lowerTile.getLevel() == currentTile.getLevel() && !lowerTile.isLevelingUp()) {
                    // If lower element is defined, check if same level and combine it, else, do nothing.
                    newMatrix[k][y] = currentTile.levelUp()
                    if (newMatrix[k - 1][y] == currentTile) {
                        newMatrix[k - 1][y] = null
                    }
                } else {
                    break
                }
            }
        }

        traverseMatrixForDown(tileMatrix) { _, _, currentTile ->
            movePendingTile(currentTile, newMatrix)
        }
        return newMatrix
    }

    private fun moveLeft(): Array<Array<Tile?>> {
        val newMatrix: Array<Array<Tile?>> = Array(gridRows) { arrayOfNulls(gridColumns) }
        traverseMatrixForLeft(tileMatrix) { x, y, currentTile ->
            // If tile present in matrix, define it in new.
            newMatrix[x][y] = currentTile
            for (k in y - 1 downTo 0) {
                // If left element is not defined, move current one step to up position.
                val leftTile = newMatrix[x][k]
                if (leftTile == null) {
                    newMatrix[x][k] = currentTile
                    if (newMatrix[x][k + 1] == currentTile) {
                        newMatrix[x][k + 1] = null
                    }
                } else if (leftTile.getLevel() == currentTile.getLevel() && !leftTile.isLevelingUp()) {
                    // If left element is defined, check if same level and combine it, else, do nothing.
                    newMatrix[x][k] = currentTile.levelUp()
                    if (newMatrix[x][k + 1] == currentTile) {
                        newMatrix[x][k + 1] = null
                    }
                } else {
                    break
                }
            }
        }

        traverseMatrixForLeft(tileMatrix) { _, _, currentTile ->
            movePendingTile(currentTile, newMatrix)
        }
        return newMatrix
    }

    private fun moveRight(): Array<Array<Tile?>> {
        val newMatrix: Array<Array<Tile?>> = Array(gridRows) { arrayOfNulls(gridColumns) }
        traverseMatrixForRight(tileMatrix) { x, y, currentTile ->
            // If tile present in matrix, define it in new.
            newMatrix[x][y] = currentTile
            for (k in y + 1..<gridColumns) {
                // If right element is not defined, move current one step to up position.
                val rightTile = newMatrix[x][k]
                if (rightTile == null) {
                    newMatrix[x][k] = currentTile
                    if (newMatrix[x][k - 1] == currentTile) {
                        newMatrix[x][k - 1] = null
                    }
                } else if (rightTile.getLevel() == currentTile.getLevel() && !rightTile.isLevelingUp()) {
                    // If right element is defined, check if same level and combine it, else, do nothing.
                    newMatrix[x][k] = currentTile.levelUp()
                    if (newMatrix[x][k - 1] == currentTile) {
                        newMatrix[x][k - 1] = null
                    }
                } else {
                    break
                }
            }
        }

        traverseMatrixForRight(tileMatrix) { _, _, currentTile ->
            movePendingTile(currentTile, newMatrix)
        }
        return newMatrix
    }

    private fun movePendingTile(
        currentTile: Tile,
        newMatrix: Array<Array<Tile?>>
    ) {
        for (a in 0..<gridColumns) {
            for (b in 0..<gridRows) {
                val currentNewMatrixTile = newMatrix[a][b]
                if (currentNewMatrixTile != null && currentNewMatrixTile == currentTile) {
                    movingTiles.plus(currentNewMatrixTile)
                    currentNewMatrixTile.move(a, b)
                    break
                }
            }
        }
    }

    private fun Array<Array<Tile?>>.forEachNonNull(action: (Tile) -> Unit) =
        this
            .iterator()
            .forEach { rows ->
                rows.forEach {
                    it?.let { action(it) }
                }
            }

    private fun traverseMatrixForUp(matrix: Array<Array<Tile?>>, action: (Int, Int, Tile) -> Unit) {
        traverseMatrix(
            matrix,
            0..<gridRows,
            0..<gridColumns,
            action
        )
    }

    private fun traverseMatrixForDown(matrix: Array<Array<Tile?>>, action: (Int, Int, Tile) -> Unit) {
        traverseMatrix(
            matrix,
            gridRows - 1 downTo 0,
            gridColumns - 1 downTo 0,
            action
        )
    }

    private fun traverseMatrixForLeft(matrix: Array<Array<Tile?>>, action: (Int, Int, Tile) -> Unit) {
        traverseMatrix(
            matrix,
            0..<gridRows,
            0..<gridColumns,
            action
        )
    }

    private fun traverseMatrixForRight(matrix: Array<Array<Tile?>>, action: (Int, Int, Tile) -> Unit) {
        traverseMatrix(
            matrix,
            0..<gridRows,
            gridColumns - 1 downTo 0,
            action
        )
    }

    private fun traverseMatrix(
        matrix: Array<Array<Tile?>>,
        xDirection: IntProgression,
        yDirection: IntProgression,
        action: (Int, Int, Tile) -> Unit
    ) {
        for (x in xDirection) {
            for (y in yDirection) {
                matrix[x][y]?.let { tile -> action(x, y, tile) }
            }
        }
    }
}
