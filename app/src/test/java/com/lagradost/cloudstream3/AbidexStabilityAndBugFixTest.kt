package com.lagradost.cloudstream3

import com.lagradost.cloudstream3.network.CloudflareKiller
import com.lagradost.cloudstream3.ui.player.VideoGenerator
import com.lagradost.cloudstream3.ui.player.SubtitleData
import com.lagradost.cloudstream3.ui.player.ExtractorUri
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.ExtractorLinkType
import com.lagradost.cloudstream3.utils.FillerEpisodeCheck
import com.lagradost.cloudstream3.utils.downloader.DownloadUtils.appendAndDontOverride
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AbidexStabilityAndBugFixTest {

    @Test
    fun testCloudflareParseCookieMapIgnoresBlankEntries() {
        val parsed = CloudflareKiller.parseCookieMap("cf_clearance=abc123; empty=; ; __cf_bm=xyz; trimmed = val ")
        assertEquals(3, parsed.size)
        assertEquals("abc123", parsed["cf_clearance"])
        assertEquals("xyz", parsed["__cf_bm"])
        assertEquals("val", parsed["trimmed"])
    }

    @Test
    fun testAppendAndDontOverridePreservesExistingCaseInsensitiveKeys() {
        val base = mapOf("User-Agent" to "CustomUA", "Accept" to "application/json")
        val fallback = mapOf("user-agent" to "FallbackUA", "Referer" to "https://example.com")
        val merged = base.appendAndDontOverride(fallback)

        assertEquals("CustomUA", merged["User-Agent"])
        assertFalse(merged.containsKey("user-agent"))
        assertEquals("application/json", merged["Accept"])
        assertEquals("https://example.com", merged["Referer"])
    }

    @Test
    fun testVideoGeneratorHasPrevAndHasNextBoundaries() {
        val generator = object : VideoGenerator<String>(listOf("ep1", "ep2", "ep3")) {
            override val hasCache: Boolean = false
            override val canSkipLoading: Boolean = false
            override fun getId(index: Int): Int? = index
            override suspend fun generateLinks(
                clearCache: Boolean,
                sourceTypes: Set<ExtractorLinkType>,
                callback: (Pair<ExtractorLink?, ExtractorUri?>) -> Unit,
                subtitleCallback: (SubtitleData) -> Unit,
                offset: Int,
                isCasting: Boolean
            ): Boolean = true
        }

        assertFalse(generator.hasPrev(0))
        assertTrue(generator.hasNext(0))
        assertTrue(generator.hasPrev(1))
        assertTrue(generator.hasNext(1))
        assertTrue(generator.hasPrev(2))
        assertFalse(generator.hasNext(2))
    }

    @Test
    fun testFillerEpisodeCheckStripNameNormalizesTitles() {
        assertEquals("attackontitan", FillerEpisodeCheck.stripName("Attack on Titan!"))
        assertEquals("rezero", FillerEpisodeCheck.stripName("Re:Zero - "))
    }
}
