package com.example.e2048

import android.graphics.Canvas
import android.view.SurfaceHolder
import java.lang.System.nanoTime

internal class MainThread(
    var surfaceHolder: SurfaceHolder,
    private val gameManager: GameManager
) : Thread() {

    private val targetFPS = 60
    private var canvas: Canvas? = null
    var running: Boolean = false

    override fun run() {
        super.run()
        var startTime: Long
        var timeMillis: Long
        var waitTime: Long
        val targetTime = (1000 / targetFPS).toLong()

        while (running) {
            startTime = nanoTime()
            canvas = null
            try {
                canvas = surfaceHolder.lockCanvas()
                synchronized(surfaceHolder) {
                    gameManager.update()
                    gameManager.draw(canvas!!)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                if (canvas != null) {
                    try {
                        surfaceHolder.unlockCanvasAndPost(canvas)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            timeMillis = (nanoTime() - startTime) / 1000000
            waitTime = targetTime - timeMillis

            try {
                if (waitTime > 0) {
                    sleep(waitTime)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}