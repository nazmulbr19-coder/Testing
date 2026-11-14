package com.lagradost.cloudstream3.ui.game

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.activity.OnBackPressedCallback
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.lagradost.cloudstream3.ui.settings.Globals.EMULATOR
import com.lagradost.cloudstream3.ui.settings.Globals.TV
import com.lagradost.cloudstream3.ui.settings.Globals.isLayout
import java.io.IOException

/**
 * Hosts the original larprad/tetris HTML/CSS/JS game offline via bundled assets
 * with TV Remote D-pad forwarding and edge-to-edge WindowInsets support.
 */
class TetrisFragment : Fragment() {

    companion object {
        private const val VIRTUAL_HOST = "appassets.local"
        private const val BASE_URL = "https://$VIRTUAL_HOST"

        internal fun mimeTypeForPath(path: String): String = when {
            path.endsWith(".html") -> "text/html"
            path.endsWith(".js") -> "application/javascript"
            path.endsWith(".css") -> "text/css"
            path.endsWith(".png") -> "image/png"
            path.endsWith(".svg") -> "image/svg+xml"
            path.endsWith(".ico") -> "image/x-icon"
            path.endsWith(".mp3") -> "audio/mpeg"
            else -> "application/octet-stream"
        }

        internal fun mapKeyToAction(keyCode: Int): String? = when (keyCode) {
            KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.KEYCODE_DPAD_DOWN_LEFT -> "left"
            KeyEvent.KEYCODE_DPAD_RIGHT, KeyEvent.KEYCODE_DPAD_DOWN_RIGHT -> "right"
            KeyEvent.KEYCODE_DPAD_UP -> "up"
            KeyEvent.KEYCODE_DPAD_DOWN -> "down"
            KeyEvent.KEYCODE_DPAD_CENTER,
            KeyEvent.KEYCODE_ENTER,
            KeyEvent.KEYCODE_NUMPAD_ENTER,
            KeyEvent.KEYCODE_SPACE -> "ok"
            KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE,
            KeyEvent.KEYCODE_MEDIA_PAUSE,
            KeyEvent.KEYCODE_MEDIA_PLAY,
            KeyEvent.KEYCODE_P,
            KeyEvent.KEYCODE_MENU,
            KeyEvent.KEYCODE_BUTTON_START -> "pause"
            KeyEvent.KEYCODE_MEDIA_REWIND,
            KeyEvent.KEYCODE_MEDIA_PREVIOUS,
            KeyEvent.KEYCODE_CHANNEL_DOWN,
            KeyEvent.KEYCODE_A -> "rotLeft"
            KeyEvent.KEYCODE_MEDIA_FAST_FORWARD,
            KeyEvent.KEYCODE_MEDIA_NEXT,
            KeyEvent.KEYCODE_CHANNEL_UP,
            KeyEvent.KEYCODE_INFO,
            KeyEvent.KEYCODE_E -> "save"
            else -> null
        }
    }

    private var webView: TetrisWebView? = null

    private val backPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            val view = webView
            if (view == null) {
                exitGame()
                return
            }
            view.evaluateJavascript("window.__abidexTetrisBack && window.__abidexTetrisBack()") { result ->
                val clean = result?.trim('"')
                if (clean != "handled") {
                    exitGame()
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val context = inflater.context
        val root = FrameLayout(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(Color.parseColor("#003063"))
        }

        // Apply safe insets so notches, status bars, and gesture bars never clip HUD controls
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        val tetrisWebView = TetrisWebView(context).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            configureWebView(this, context)
        }
        webView = tetrisWebView
        root.addView(tetrisWebView)

        val isTv = isLayout(TV or EMULATOR)
        val entryUrl = if (isTv) "$BASE_URL/index.html?tv=1" else "$BASE_URL/index.html"
        tetrisWebView.loadUrl(entryUrl)
        tetrisWebView.requestFocus()

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            backPressedCallback
        )
    }

    override fun onResume() {
        super.onResume()
        webView?.onResume()
        webView?.requestFocus()
    }

    override fun onPause() {
        webView?.evaluateJavascript(
            "window.__abidexTetrisPauseAudio && window.__abidexTetrisPauseAudio()",
            null
        )
        webView?.onPause()
        super.onPause()
    }

    override fun onDestroyView() {
        webView?.let { view ->
            view.stopLoading()
            view.destroy()
        }
        webView = null
        super.onDestroyView()
    }

    private fun exitGame() {
        backPressedCallback.isEnabled = false
        findNavController().popBackStack()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun configureWebView(view: WebView, context: Context) {
        view.setBackgroundColor(Color.parseColor("#003063"))
        view.isFocusable = true
        view.isFocusableInTouchMode = true
        view.overScrollMode = View.OVER_SCROLL_NEVER

        view.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            mediaPlaybackRequiresUserGesture = false
            cacheMode = WebSettings.LOAD_DEFAULT
            useWideViewPort = true
            loadWithOverviewMode = true
            setSupportZoom(false)
            builtInZoomControls = false
            displayZoomControls = false
        }

        view.webViewClient = object : WebViewClient() {
            override fun shouldInterceptRequest(
                view: WebView?,
                request: WebResourceRequest?
            ): WebResourceResponse? {
                val url = request?.url ?: return null
                if (url.host != VIRTUAL_HOST) return null
                val rawPath = url.path?.trimStart('/').orEmpty().ifEmpty { "index.html" }
                return serveTetrisAsset(context, rawPath)
            }

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                // Keep gameplay offline inside the local Tetris bundle
                return request?.url?.host != VIRTUAL_HOST
            }
        }
    }

    private fun serveTetrisAsset(context: Context, assetSubPath: String): WebResourceResponse? {
        val cleanPath = assetSubPath.substringBefore('?').substringBefore('#')
        if (cleanPath.contains("..")) return null

        return try {
            val stream = context.assets.open("tetris/$cleanPath")
            WebResourceResponse(mimeTypeForPath(cleanPath), "UTF-8", stream)
        } catch (_: IOException) {
            null
        }
    }

    private class TetrisWebView(context: Context) : WebView(context) {
        override fun dispatchKeyEvent(event: KeyEvent): Boolean {
            val actionName = mapKeyToAction(event.keyCode)
            if (actionName != null) {
                if (event.action == KeyEvent.ACTION_DOWN) {
                    evaluateJavascript(
                        "window.__abidexTetrisKey && window.__abidexTetrisKey('$actionName', ${event.repeatCount})",
                        null
                    )
                }
                // Consume both ACTION_DOWN and ACTION_UP so Android focus never jumps away
                return true
            }
            return super.dispatchKeyEvent(event)
        }
    }
}
