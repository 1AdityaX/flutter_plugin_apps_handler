package dev.aditya.apps_handler

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Build
import java.io.ByteArrayOutputStream

data class AppInfo(
    val name: String,
    val packageName: String,
    val category: String,
    val versionName: String?,
    val versionCode: Long,
    val dataDir: String,
    val systemApp: Boolean,
    val installerPackageName: String?,
    private val drawable: Drawable?,
    val enabled: Boolean,
    val installTimeMillis: Long,
    val updateTimeMillis: Long,
) {
    companion object {
        fun createFromPackageInfo(
            context: Context,
            packageInfo: PackageInfo,
            loadIcon: Boolean = false
        ): AppInfo? {
            return try {
                val packageManager = context.packageManager
                val applicationInfo = packageInfo.applicationInfo ?: return null
                val category = getApplicationCategory(applicationInfo)

                AppInfo(
                    name = applicationInfo.loadLabel(packageManager).toString(),
                    packageName = packageInfo.packageName,
                    category = category,
                    versionName = packageInfo.versionName,
                    versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        packageInfo.longVersionCode
                    } else {
                        @Suppress("DEPRECATION")
                        packageInfo.versionCode.toLong()
                    },
                    dataDir = applicationInfo.dataDir,
                    systemApp = (applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0,
                    installerPackageName = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        packageManager.getInstallSourceInfo(packageInfo.packageName).installingPackageName
                    } else {
                        @Suppress("DEPRECATION")
                        packageManager.getInstallerPackageName(packageInfo.packageName)
                    },
                    drawable = if (loadIcon) applicationInfo.loadIcon(packageManager) else null,
                    enabled = applicationInfo.enabled,
                    installTimeMillis = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        packageInfo.firstInstallTime
                    } else {
                        @Suppress("DEPRECATION")
                        packageInfo.firstInstallTime
                    },
                    updateTimeMillis = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        packageInfo.lastUpdateTime
                    } else {
                        @Suppress("DEPRECATION")
                        packageInfo.lastUpdateTime
                    }
                )
            } catch (e: Exception) {
                null
            }
        }

        private fun getApplicationCategory(applicationInfo: ApplicationInfo): String {
            return when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                    when (applicationInfo.category) {
                        ApplicationInfo.CATEGORY_GAME -> "game"
                        ApplicationInfo.CATEGORY_AUDIO -> "audio"
                        ApplicationInfo.CATEGORY_VIDEO -> "video"
                        ApplicationInfo.CATEGORY_IMAGE -> "image"
                        ApplicationInfo.CATEGORY_SOCIAL -> "social"
                        ApplicationInfo.CATEGORY_NEWS -> "news"
                        ApplicationInfo.CATEGORY_MAPS -> "maps"
                        ApplicationInfo.CATEGORY_PRODUCTIVITY -> "productivity"
                        else -> "undefined"
                    }
                }
                else -> "undefined"
            }
        }
    }

    fun toMap(includeIcon: Boolean = false): Map<String, Any?> {
        val map = mutableMapOf<String, Any?>( 
            "app_name" to name,
            "package_name" to packageName,
            "category" to category,
            "version_name" to versionName,
            "version_code" to versionCode,
            "data_dir" to dataDir,
            "system_app" to systemApp,
            "installer_package_name" to installerPackageName,
            "enabled" to enabled,
            "install_time" to installTimeMillis,
            "update_time" to updateTimeMillis
        )

        if (includeIcon && drawable != null) {
            map["app_icon"] = drawableToByteArray(drawable)
        }

        return map
    }

    private fun drawableToByteArray(drawable: Drawable): ByteArray {
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)

        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        val byteArray = stream.toByteArray()
        bitmap.recycle()
        return byteArray
    }
}
