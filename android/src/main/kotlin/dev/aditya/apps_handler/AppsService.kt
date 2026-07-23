package dev.aditya.apps_handler

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppsService(private val context: Context) {
    private val packageManager: PackageManager = context.packageManager

    suspend fun getInstalledApps(
        includeSystemApps: Boolean = false,
        includeAppIcons: Boolean = false,
        onlyAppsWithLaunchIntent: Boolean = false
    ): List<Map<String, Any?>> = withContext(Dispatchers.IO) {
        val installedApps = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getInstalledPackages(PackageManager.PackageInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            packageManager.getInstalledPackages(0)
        }
        val launchActivities = getLaunchActivities()

        installedApps
            .filter { packageInfo ->
                val applicationInfo = packageInfo.applicationInfo
                applicationInfo != null &&
                    (includeSystemApps ||
                        applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM == 0) &&
                    (!onlyAppsWithLaunchIntent ||
                        launchActivities.containsKey(packageInfo.packageName))
            }
            .mapNotNull { packageInfo ->
                AppInfo.createFromPackageInfo(
                    context,
                    packageInfo,
                    launchActivities[packageInfo.packageName],
                    includeAppIcons
                )
            }
            .map { it.toMap(includeAppIcons) }
    }

    suspend fun getApp(
        packageName: String,
        includeAppIcon: Boolean = false
    ): Map<String, Any?>? = withContext(Dispatchers.IO) {
        try {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                packageManager.getPackageInfo(packageName, 0)
            }

            AppInfo.createFromPackageInfo(
                context,
                packageInfo,
                getLaunchActivities(packageName)[packageName],
                loadIcon = includeAppIcon
            )?.toMap(includeAppIcon)
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }

    fun launchApp(packageName: String, activityName: String?): Boolean {
        val intent = activityName?.let {
            Intent.makeMainActivity(ComponentName(packageName, it)).apply {
                addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
            }
        }
        return launch(intent) || launch(packageManager.getLaunchIntentForPackage(packageName))
    }

    suspend fun isAppInstalled(packageName: String): Boolean = withContext(Dispatchers.IO) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                packageManager.getPackageInfo(packageName, 0)
            }
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    fun uninstallApp(packageName: String): Boolean {
        return try {
            context.startActivity(
                Intent(Intent.ACTION_DELETE).apply {
                    data = Uri.parse("package:$packageName")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            )
            true
        } catch (_: Exception) {
            false
        }
    }

    fun openAppSettings(packageName: String): Boolean {
        return try {
            context.startActivity(
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("package:$packageName")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            )
            true
        } catch (_: Exception) {
            false
        }
    }

    private fun getLaunchActivities(packageName: String? = null): Map<String, String> {
        val intent = Intent(Intent.ACTION_MAIN)
            .addCategory(Intent.CATEGORY_LAUNCHER)
            .setPackage(packageName)
        val activities = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.queryIntentActivities(
                intent,
                PackageManager.ResolveInfoFlags.of(0)
            )
        } else {
            @Suppress("DEPRECATION")
            packageManager.queryIntentActivities(intent, 0)
        }

        val launchActivities = mutableMapOf<String, String>()
        for (resolveInfo in activities) {
            val activityInfo = resolveInfo.activityInfo ?: continue
            if (!launchActivities.containsKey(activityInfo.packageName)) {
                launchActivities[activityInfo.packageName] = activityInfo.name
            }
        }
        return launchActivities
    }

    private fun launch(intent: Intent?): Boolean {
        if (intent == null) return false
        return try {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            false
        }
    }
}
