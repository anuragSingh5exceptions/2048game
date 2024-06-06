package com.example.myapplication

import android.os.Bundle
import android.preference.PreferenceManager
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.WindowManager
import android.webkit.JavascriptInterface
import android.webkit.WebSettings.RenderPriority
import android.webkit.WebView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.util.Locale
import kotlin.math.abs

class MainActivity : AppCompatActivity() {
    private val MAIN_ACTIVITY_TAG = "2048_MainActivity"

    lateinit var mWebView: WebView
    private var mLastBackPress: Long = 0
    private val mBackPressThreshold: Long = 3500
    private val IS_FULLSCREEN_PREF = "is_fullscreen_pref"
    private var mLastTouch: Long = 0
    private val mTouchThreshold: Long = 2000
    private var pressBackToast: Toast? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

//        val changeLog: DialogChangeLog = DialogChangeLog.newInstance(this)
//        if (changeLog.isFirstRun()) {
//            changeLog.getLogDialog().show()
//        }


        // Load webview with game
        mWebView = findViewById(R.id.mainWebView)
        val settings = mWebView.getSettings()
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.databaseEnabled = true
        settings.setRenderPriority(RenderPriority.HIGH)
        settings.databasePath = filesDir.parentFile.path + "/databases"
        mWebView.addJavascriptInterface(WebAppInterface(this), "Android")

        // If there is a previous instance restore it in the webview
        if (savedInstanceState != null) {
            mWebView.restoreState(savedInstanceState)
        } else {
            // Load webview with current Locale language
            mWebView.loadUrl("file:///android_asset/2048/index.html?lang=" + Locale.getDefault().language)
        }

        Toast.makeText(application, "toggle_fullscreen", Toast.LENGTH_SHORT).show()

        // Set fullscreen toggle on webview LongClick
        mWebView.setOnTouchListener(OnTouchListener { v: View?, event: MotionEvent ->
            // Implement a long touch action by comparing
            // time between action up and action down
            val currentTime = System.currentTimeMillis()
            if ((event.action == MotionEvent.ACTION_UP)
                && (abs((currentTime - mLastTouch).toDouble()) > mTouchThreshold)
            ) {
                val toggledFullScreen: Boolean = !isFullScreen()
                saveFullScreen(toggledFullScreen)
                applyFullScreen(toggledFullScreen)
            } else if (event.action == MotionEvent.ACTION_DOWN) {
                mLastTouch = currentTime
            }
            false
        })

        pressBackToast = Toast.makeText(
            applicationContext, "press_back_again_to_exit",
            Toast.LENGTH_SHORT
        )
    }

    class WebAppInterface(private val activity: MainActivity) {
        @JavascriptInterface
        fun sendScoreToKotlin(score: String) {
            // Handle the score received from JavaScript
            activity.runOnUiThread {
                Toast.makeText(activity, "Score: $score", Toast.LENGTH_SHORT).show()

                // You can update your UI or perform other operations with the score
            }
        }
    }

    private fun isFullScreen(): Boolean {
        return PreferenceManager.getDefaultSharedPreferences(this).getBoolean(
            IS_FULLSCREEN_PREF,
            true
        )
    }
    private fun saveFullScreen(isFullScreen: Boolean) {
        // save in preferences
        val editor = PreferenceManager.getDefaultSharedPreferences(this).edit()
        editor.putBoolean(IS_FULLSCREEN_PREF, isFullScreen)
        editor.apply()
    }
    private fun applyFullScreen(isFullScreen: Boolean) {
        if (isFullScreen) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
    }

    override fun onBackPressed() {

        val currentTime = System.currentTimeMillis()
        if (abs((currentTime - mLastBackPress).toDouble()) > mBackPressThreshold) {
            pressBackToast!!.show()
            mLastBackPress = currentTime
        } else {
            pressBackToast!!.cancel()
            super.onBackPressed()
        }
    }

    override fun onResume() {
        super.onResume()
        mWebView.loadUrl("file:///android_asset/2048/index.html?lang=" + Locale.getDefault().language)

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mWebView.saveState(outState)
    }
}