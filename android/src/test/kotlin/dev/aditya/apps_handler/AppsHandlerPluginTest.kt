package dev.aditya.apps_handler

import kotlin.test.Test
import kotlin.test.assertEquals

internal class AppsHandlerPluginTest {
    @Test
    fun appInfoIncludesActivityName() {
        val app = AppInfo(
            name = "Example",
            packageName = "com.example",
            activityName = "com.example.MainActivity",
            category = "undefined",
            versionName = null,
            versionCode = 1,
            dataDir = "",
            systemApp = false,
            installerPackageName = null,
            drawable = null,
            enabled = true,
            installTimeMillis = 0,
            updateTimeMillis = 0
        )

        assertEquals("com.example.MainActivity", app.toMap()["activity_name"])
    }
}
