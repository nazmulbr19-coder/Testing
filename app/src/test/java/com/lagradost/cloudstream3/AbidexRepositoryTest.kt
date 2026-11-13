package com.lagradost.cloudstream3

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.lagradost.cloudstream3.plugins.Repository
import com.lagradost.cloudstream3.plugins.RepositoryManager
import com.lagradost.cloudstream3.plugins.SitePlugin
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AbidexRepositoryTest {

    @Test
    fun testAbidexRepositoryUrlConfiguration() {
        assertEquals(
            "https://raw.githubusercontent.com/nazmulbr19-coder/abidstream/builds/repo.json",
            RepositoryManager.ABIDEX_REPO_URL
        )
        assertEquals("ABIDEX", RepositoryManager.ABIDEX_REPO_NAME)
        assertTrue(
            RepositoryManager.PREBUILT_REPOSITORIES.any { it.url == RepositoryManager.ABIDEX_REPO_URL }
        )
    }

    @Test
    fun testRepoManifestParsing() {
        val json = """
            {
              "name": "abidstreaming",
              "description": "abidstreaming CloudStream extensions repository",
              "manifestVersion": 1,
              "pluginLists": [
                "https://raw.githubusercontent.com/nazmulbr19-coder/abidstream/builds/plugins.json"
              ]
            }
        """.trimIndent()

        val mapper = jacksonObjectMapper()
        val repo = mapper.readValue<Repository>(json)

        assertEquals("abidstreaming", repo.name)
        assertEquals(1, repo.manifestVersion)
        assertEquals(1, repo.pluginLists.size)
        assertEquals(
            "https://raw.githubusercontent.com/nazmulbr19-coder/abidstream/builds/plugins.json",
            repo.pluginLists[0]
        )
    }

    @Test
    fun testPluginsManifestParsing() {
        val json = """
            [
              {
                "url": "https://raw.githubusercontent.com/nazmulbr19-coder/abidstream/builds/AniChan.cs3",
                "status": 1,
                "version": 18,
                "name": "AniChan",
                "internalName": "AniChan",
                "authors": ["abidstreaming"],
                "description": "Watch Anime in HD with Sub, Dub and Hardsub",
                "fileSize": 81537,
                "repositoryUrl": "https://github.com/nazmulbr19-coder/abidstream",
                "language": "en",
                "tvTypes": ["Anime", "AnimeMovie"],
                "iconUrl": "https://play-lh.googleusercontent.com/test.png",
                "apiVersion": 1,
                "fileHash": "sha256-2ab2fac8c64abe91ed58758b74b45d19f68b6df00f874b1e436d0c427dd367c8"
              },
              {
                "url": "https://raw.githubusercontent.com/nazmulbr19-coder/abidstream/builds/AniKotoAnime.cs3",
                "status": 1,
                "version": 21,
                "name": "AniKotoAnime",
                "internalName": "AniKotoAnime",
                "authors": ["abidstreaming"],
                "description": "AniKoto Anime",
                "fileSize": 62735,
                "repositoryUrl": "https://github.com/nazmulbr19-coder/abidstream",
                "language": "en",
                "tvTypes": ["Anime"],
                "iconUrl": "https://anikototv.to/favicon.png",
                "apiVersion": 1,
                "fileHash": "sha256-068ce2b5a83b559b1849341e33a01a4aa3db5fec3f3a1791fd71c6d6ba7e1934"
              }
            ]
        """.trimIndent()

        val mapper = jacksonObjectMapper()
        val plugins = mapper.readValue<List<SitePlugin>>(json)

        assertEquals(2, plugins.size)
        val anichan = plugins.firstOrNull { it.internalName == "AniChan" }
        assertNotNull(anichan)
        assertEquals(18, anichan?.version)
        assertEquals("sha256-2ab2fac8c64abe91ed58758b74b45d19f68b6df00f874b1e436d0c427dd367c8", anichan?.fileHash)
    }

    @Test
    fun testAllFiveBuiltInPluginsManifest() {
        val json = """
            [
              {
                "url": "https://raw.githubusercontent.com/nazmulbr19-coder/abidstream/builds/AniChan.cs3",
                "status": 1,
                "version": 18,
                "name": "AniChan",
                "internalName": "AniChan",
                "authors": ["abidstreaming"],
                "description": "Watch Anime in HD with Sub, Dub and Hardsub",
                "fileSize": 81537,
                "repositoryUrl": "https://github.com/nazmulbr19-coder/abidstream",
                "language": "en",
                "tvTypes": ["Anime", "AnimeMovie"],
                "iconUrl": "https://play-lh.googleusercontent.com/test.png",
                "apiVersion": 1,
                "fileHash": "sha256-2ab2fac8c64abe91ed58758b74b45d19f68b6df00f874b1e436d0c427dd367c8"
              },
              {
                "url": "https://raw.githubusercontent.com/nazmulbr19-coder/abidstream/builds/AniKotoAnime.cs3",
                "status": 1,
                "version": 21,
                "name": "AniKotoAnime",
                "internalName": "AniKotoAnime",
                "authors": ["abidstreaming"],
                "description": "AniKoto Anime - Watch Anime Sub & Dub in HD",
                "fileSize": 62735,
                "repositoryUrl": "https://github.com/nazmulbr19-coder/abidstream",
                "language": "en",
                "tvTypes": ["Anime", "AnimeMovie", "OVA"],
                "iconUrl": "https://anikototv.to/AnikotoTheme/assets/images/favicon.png",
                "apiVersion": 1,
                "fileHash": "sha256-068ce2b5a83b559b1849341e33a01a4aa3db5fec3f3a1791fd71c6d6ba7e1934"
              },
              {
                "url": "https://raw.githubusercontent.com/nazmulbr19-coder/abidstream/builds/AniSuge.cs3",
                "status": 1,
                "version": 10,
                "name": "AniSuge",
                "internalName": "AniSuge",
                "authors": ["abidstreaming"],
                "description": "Anime from AniSuge with Sub and Dub support",
                "fileSize": 77014,
                "repositoryUrl": "https://github.com/nazmulbr19-coder/abidstream",
                "language": "en",
                "tvTypes": ["Anime", "AnimeMovie", "OVA"],
                "iconUrl": "https://animesuge.cz/animesuge/images/logo.png",
                "apiVersion": 1,
                "fileHash": "sha256-62983888354e6eb0df775807727be904fd4e993e346a88b0de30ff3bd7bb91b4"
              },
              {
                "url": "https://raw.githubusercontent.com/nazmulbr19-coder/abidstream/builds/RareAnimesIndia.cs3",
                "status": 1,
                "version": 9,
                "name": "RareAnimesIndia",
                "internalName": "RareAnimesIndia",
                "authors": ["abidstreaming"],
                "description": "hindi cartoon",
                "fileSize": 166239,
                "repositoryUrl": "https://github.com/nazmulbr19-coder/abidstream",
                "language": "hi",
                "tvTypes": ["Anime", "Cartoon", "TvSeries", "Movie"],
                "iconUrl": "https://www.rareanimes.mov/wp-content/uploads/2023/11/cropped-Rare-Animes-India.png",
                "apiVersion": 1,
                "fileHash": "sha256-cc031f2394153ce01a5a1ae49419dbd0258aa355770f66a09fff951bec7efa54"
              },
              {
                "url": "https://raw.githubusercontent.com/nazmulbr19-coder/abidstream/builds/Toonix.cs3",
                "status": 1,
                "version": 9,
                "name": "Toonix",
                "internalName": "Toonix",
                "authors": ["abidstreaming"],
                "description": "cartoons and anime in hindi, english and japanese sub",
                "fileSize": 91643,
                "repositoryUrl": "https://github.com/nazmulbr19-coder/abidstream",
                "language": "hi",
                "tvTypes": ["Cartoon", "Anime", "Movie", "TvSeries"],
                "iconUrl": "https://toonix.bond/icon.png?df3a3feb46702fd9",
                "apiVersion": 1,
                "fileHash": "sha256-ab0ae2261f88c995d52c48122fbf525c98b90d25a8a375eef70ce28df41dd2e6"
              }
            ]
        """.trimIndent()

        val mapper = jacksonObjectMapper()
        val plugins = mapper.readValue<List<SitePlugin>>(json)

        assertEquals(5, plugins.size)
        val names = plugins.map { it.internalName }.toSet()
        assertTrue(names.contains("AniChan"))
        assertTrue(names.contains("AniKotoAnime"))
        assertTrue(names.contains("AniSuge"))
        assertTrue(names.contains("RareAnimesIndia"))
        assertTrue(names.contains("Toonix"))
    }

    @Test
    fun testDefaultProviderIsAniChan() {
        assertEquals(
            "AniChan",
            com.lagradost.cloudstream3.utils.DataStoreHelper.DEFAULT_HOMEPAGE_API
        )
    }

    @Test
    fun testRemovedSettingsSectionsNotRegistered() {
        val screenTitles = com.lagradost.cloudstream3.ui.settings.SettingsFragmentScreen.screens.map { it.title }
        org.junit.Assert.assertFalse(screenTitles.contains(R.string.category_updates))
        val generalSubtitles = com.lagradost.cloudstream3.ui.settings.SettingsFragmentScreen.screens
            .first { it.title == R.string.category_general }
            .subtitle
        org.junit.Assert.assertFalse(generalSubtitles.contains(R.string.pref_category_links))
    }

    @Test
    fun testAutoPluginUpdateDetectsNewVersionAndAlwaysUpdate() {
        val repoData = com.lagradost.cloudstream3.ui.settings.extensions.RepositoryData(
            iconUrl = null,
            name = "ABIDEX",
            url = RepositoryManager.ABIDEX_REPO_URL,
            description = null
        )
        val repo = Repository(
            iconUrl = null,
            name = "ABIDEX",
            description = null,
            manifestVersion = 1,
            pluginLists = emptyList()
        )
        val saved = com.lagradost.cloudstream3.plugins.PluginData(
            internalName = "AniChan",
            url = "https://example.com/AniChan.cs3",
            isOnline = true,
            filePath = "/tmp/AniChan.cs3",
            version = 18
        )
        val newerRemote = SitePlugin(
            url = "https://example.com/AniChan.cs3",
            status = 1,
            version = 19,
            apiVersion = 1,
            name = "AniChan",
            internalName = "AniChan",
            authors = emptyList(),
            description = null,
            repositoryUrl = null,
            tvTypes = null,
            language = "en",
            iconUrl = null,
            fileSize = null,
            fileHash = null
        )
        val sameRemote = newerRemote.copy(version = 18)
        val alwaysUpdateRemote = newerRemote.copy(version = com.lagradost.cloudstream3.plugins.PLUGIN_VERSION_ALWAYS_UPDATE)

        assertTrue(
            com.lagradost.cloudstream3.plugins.PluginManager.OnlinePluginData(
                saved,
                com.lagradost.cloudstream3.plugins.PluginWrapper(repo, repoData, newerRemote)
            ).isOutdated
        )
        org.junit.Assert.assertFalse(
            com.lagradost.cloudstream3.plugins.PluginManager.OnlinePluginData(
                saved,
                com.lagradost.cloudstream3.plugins.PluginWrapper(repo, repoData, sameRemote)
            ).isOutdated
        )
        assertTrue(
            com.lagradost.cloudstream3.plugins.PluginManager.OnlinePluginData(
                saved,
                com.lagradost.cloudstream3.plugins.PluginWrapper(repo, repoData, alwaysUpdateRemote)
            ).isOutdated
        )
    }
}
