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
    fun testCarConnectionProviderConstants() {
        assertEquals("CarConnectionState", CarConnectionProvider.COLUMN_CAR_CONNECTION_STATE)
        assertEquals(0, CarConnectionProvider.CONNECTION_TYPE_NOT_CONNECTED)
        assertNotNull(CarConnectionProvider())
    }
}
