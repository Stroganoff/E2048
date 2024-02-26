package com.example.e2048

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Insets
import android.os.Build
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.util.Log
import android.view.MotionEvent
import android.view.MotionEvent.ACTION_DOWN
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.WindowInsets
import android.widget.Toast
import com.example.e2048.sprites.EndGame
import com.example.e2048.sprites.Grid
import com.example.e2048.sprites.Score
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.admanager.AdManagerAdRequest
import com.google.android.gms.ads.admanager.AdManagerInterstitialAd
import com.google.android.gms.ads.admanager.AdManagerInterstitialAdLoadCallback

internal class GameManager(
    private val context: Context,
    attrs: AttributeSet
) : SurfaceView(context, attrs), SurfaceHolder.Callback, SwipeCallback,
    GameManagerCallback {

    private var screenWidth: Int = 0
    private var screenHeight: Int = 0
    private var standardSize: Int = 0

    private var gridRows = 4
    private var gridColumns = 4
    private var initialNumberOfTiles = 2

    init {
        isLongClickable = true
        holder.addCallback(this)
        getScreenSize(context as Activity)
        standardSize = (screenWidth * .88 / 4).toInt()
    }

    private lateinit var thread: MainThread
    private val sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_ID, MODE_PRIVATE)
    private val tileManager =
        TileManager(resources, screenWidth, screenHeight, standardSize, gridRows, gridColumns, initialNumberOfTiles, this)
    private val grid: Grid = Grid(resources, screenWidth, screenHeight, standardSize)
    private val endgame = EndGame(resources, screenWidth, screenHeight)
    private lateinit var score: Score

    private val restartButtonSize = resources.getDimension(R.dimen.restart_button_size).toInt()
    private val restartButton = BitmapFactory.decodeResource(resources, R.drawable.restart).let {
        Bitmap.createScaledBitmap(it, restartButtonSize, restartButtonSize, false)
    }
    private val restartButtonX = screenWidth / 2f + 2f * standardSize - restartButtonSize
    private val restartButtonY = screenHeight / 2f - 2f * standardSize - 3f * restartButtonSize / 2f

    private val swipeListener = SwipeListener(context, this)

    private var interstitialAd: AdManagerInterstitialAd? = null
    private var adIsLoading = false

    private var isEndgame = true

    init {
        initGame()
    }

    private fun initGame() {
        isEndgame = false
        interstitialAd = null
        tileManager.initGame()
        score = Score(resources, screenWidth, screenHeight, standardSize, sharedPreferences)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        thread = MainThread(holder, this)
        thread.running = true
        thread.start()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        thread.surfaceHolder = holder
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        var retry = true
        while (retry) {
            try {
                thread.join()
                retry = false
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
    }

    fun update() {
        if (!isEndgame) {
            tileManager.update()
        }
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        drawWhiteBackground(canvas)
        grid.draw(canvas)
        tileManager.draw(canvas)
        score.draw(canvas)
        drawRestartButton(canvas)
        if (isEndgame) {
            endgame.draw(canvas)
        }
    }

    private fun drawWhiteBackground(canvas: Canvas) {
        canvas.drawRGB(255, 255, 255)
    }

    private fun drawRestartButton(canvas: Canvas) {
        canvas.drawBitmap(restartButton, restartButtonX, restartButtonY, null)
    }

    private fun getScreenSize(activity: Activity) =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics = activity.windowManager.currentWindowMetrics
            val insets: Insets = windowMetrics.windowInsets
                .getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
            screenWidth = windowMetrics.bounds.width() - insets.left - insets.right
            screenHeight = windowMetrics.bounds.height() - insets.top - insets.bottom
        } else {
            val displayMetrics = DisplayMetrics()
            activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
            screenWidth = displayMetrics.widthPixels
            screenHeight = displayMetrics.heightPixels
        }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?) =
        event?.let {
            if (isEndgame) {
                if (it.action == ACTION_DOWN) {
                    initGame()
                }
            } else if (resetButtonWasPressed(event)) {
                initGame()
            } else {
                swipeListener.onTouchEvent(it)
            }
            return super.onTouchEvent(event)
        } ?: false

    private fun resetButtonWasPressed(event: MotionEvent) = event.action == ACTION_DOWN
            && event.x > restartButtonX
            && event.x < restartButtonX + restartButtonSize
            && event.y > restartButtonY
            && event.y < restartButtonY + restartButtonSize

    override fun onSwipe(direction: SwipeCallback.Direction) = tileManager.onSwipe(direction)

    override fun gameOver() {
        isEndgame = true
        loadInterstitialAd()
    }

    override fun updateScore(newPoints: Int) {
        score.updateScore(newPoints)
    }

    override fun reached2048() {
        score.reached2048()
    }

    private fun loadInterstitialAd() {
        // Request a new ad if one isn't already loaded.
        if (adIsLoading || interstitialAd != null) {
            return
        }
        adIsLoading = true

        (context as Activity).runOnUiThread {
            val adRequest = AdManagerAdRequest.Builder().build()
            AdManagerInterstitialAd.load(
                context,
                AD_UNIT_ID,
                adRequest,
                object : AdManagerInterstitialAdLoadCallback() {
                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        interstitialAd = null
                        adIsLoading = false
                        val error = "domain: ${adError.domain}, code: ${adError.code}, " + "message: ${adError.message}"
                        Toast.makeText(context, "onAdFailedToLoad() with error $error", Toast.LENGTH_SHORT).show()
                    }

                    override fun onAdLoaded(interstitialAd: AdManagerInterstitialAd) {
                        Log.d(TAG, "Ad was loaded.")
                        this@GameManager.interstitialAd = interstitialAd
                        interstitialAd.show(this@GameManager.context)
                        adIsLoading = false

                    }
                }
            )
        }
    }

    companion object {
        private const val SHARED_PREFERENCES_ID = "E2028"
        private const val AD_UNIT_ID = "/6499/example/interstitial"
    }
}