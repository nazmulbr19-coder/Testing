package com.lagradost.cloudstream3.ui.game

import android.view.KeyEvent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

class TetrisIntegrationTest {

    private fun resolveProjectFile(relativePath: String): File {
        val direct = File(relativePath)
        if (direct.exists()) return direct
        val parent = File("..", relativePath)
        if (parent.exists()) return parent
        return File("app", relativePath)
    }

    @Test
    fun `bottom_nav_menu has 6 items in exact expected order`() {
        val menuFile = resolveProjectFile("src/main/res/menu/bottom_nav_menu.xml")
            .takeIf { it.exists() }
            ?: resolveProjectFile("app/src/main/res/menu/bottom_nav_menu.xml")
        assertTrue("Expected bottom_nav_menu.xml to exist at ${menuFile.absolutePath}", menuFile.exists())

        val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(menuFile)
        val items = doc.getElementsByTagName("item")
        val ids = (0 until items.length).map { idx ->
            items.item(idx).attributes.getNamedItem("android:id").nodeValue.substringAfterLast('/')
        }

        assertEquals(
            listOf(
                "navigation_home",
                "navigation_search",
                "navigation_library",
                "navigation_downloads",
                "navigation_tetris",
                "navigation_settings"
            ),
            ids
        )
    }

    @Test
    fun `original larprad tetris offline assets are bundled and free of external fonts`() {
        val assetsDir = resolveProjectFile("src/main/assets/tetris")
            .takeIf { it.exists() }
            ?: resolveProjectFile("app/src/main/assets/tetris")
        assertTrue("Expected assets/tetris directory", assetsDir.isDirectory)

        val requiredAssets = listOf(
            "index.html",
            "main.js",
            "tv-bridge.js",
            "styles/style.css",
            "styles/tv-adaptive.css",
            "img/logo2.png",
            "img/grid.png",
            "img/tile.png",
            "sound/origTheme.mp3",
            "sound/samples/line.mp3"
        )
        for (asset in requiredAssets) {
            assertTrue("Missing asset $asset", File(assetsDir, asset).exists())
        }

        val indexHtml = File(assetsDir, "index.html").readText()
        assertFalse("External Google Fonts CSS should not block offline loading", indexHtml.contains("fonts.googleapis.com"))
        assertTrue(indexHtml.contains("./tv-bridge.js"))
        assertTrue(indexHtml.contains("./styles/tv-adaptive.css"))
    }

    @Test
    fun `mime types and TV remote D-pad key bindings map cleanly`() {
        assertEquals("text/html", TetrisFragment.mimeTypeForPath("index.html"))
        assertEquals("application/javascript", TetrisFragment.mimeTypeForPath("main.js"))
        assertEquals("text/css", TetrisFragment.mimeTypeForPath("styles/style.css"))
        assertEquals("image/png", TetrisFragment.mimeTypeForPath("img/logo2.png"))
        assertEquals("audio/mpeg", TetrisFragment.mimeTypeForPath("sound/origTheme.mp3"))

        assertEquals("left", TetrisFragment.mapKeyToAction(KeyEvent.KEYCODE_DPAD_LEFT))
        assertEquals("right", TetrisFragment.mapKeyToAction(KeyEvent.KEYCODE_DPAD_RIGHT))
        assertEquals("up", TetrisFragment.mapKeyToAction(KeyEvent.KEYCODE_DPAD_UP))
        assertEquals("down", TetrisFragment.mapKeyToAction(KeyEvent.KEYCODE_DPAD_DOWN))
        assertEquals("ok", TetrisFragment.mapKeyToAction(KeyEvent.KEYCODE_DPAD_CENTER))
        assertEquals("pause", TetrisFragment.mapKeyToAction(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE))
        assertEquals("save", TetrisFragment.mapKeyToAction(KeyEvent.KEYCODE_MEDIA_FAST_FORWARD))
        assertNull(TetrisFragment.mapKeyToAction(KeyEvent.KEYCODE_BACK))
    }
}
