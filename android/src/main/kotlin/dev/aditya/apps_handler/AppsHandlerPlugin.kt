package dev.aditya.apps_handler

import android.content.Context
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class AppsHandlerPlugin : FlutterPlugin, MethodCallHandler {
    private lateinit var context: Context
    private lateinit var methodChannel: MethodChannel
    private lateinit var eventChannel: EventChannel
    private lateinit var appsService: AppsService
    private lateinit var applicationChangeListener: ApplicationChangeListener
    private lateinit var scope: CoroutineScope

    override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
        context = flutterPluginBinding.applicationContext
        appsService = AppsService(context)
        applicationChangeListener = ApplicationChangeListener(context)

        methodChannel = MethodChannel(flutterPluginBinding.binaryMessenger, "apps_handler")
        methodChannel.setMethodCallHandler(this)

        eventChannel = EventChannel(flutterPluginBinding.binaryMessenger, "apps_handler/apps_channel")
        eventChannel.setStreamHandler(applicationChangeListener)
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        methodChannel.setMethodCallHandler(null)
        applicationChangeListener.dispose()
        eventChannel.setStreamHandler(null)
        scope.cancel()
    }

    override fun onMethodCall(call: MethodCall, result: Result) {
        scope.launch {
            try {
                when (call.method) {
                    "getInstalledApps" -> {
                        val includeSystemApps = call.argument<Boolean>("include_system_apps") ?: false
                        val includeAppIcons = call.argument<Boolean>("include_app_icons") ?: false
                        val onlyAppsWithLaunchIntent = call.argument<Boolean>("only_apps_with_launch_intent") ?: false

                        val apps = appsService.getInstalledApps(
                            includeSystemApps,
                            includeAppIcons,
                            onlyAppsWithLaunchIntent
                        )
                        result.success(apps)
                    }
                    "getApp" -> {
                        val packageName = call.argument<String>("package_name")
                        val includeAppIcon = call.argument<Boolean>("include_app_icon") ?: false

                        if (packageName == null) {
                            result.error("INVALID_ARGUMENT", "Package name is required", null)
                            return@launch
                        }

                        val app = appsService.getApp(packageName, includeAppIcon)
                        if (app != null) {
                            result.success(app)
                        } else {
                            result.error("APP_NOT_FOUND", "App not found", null)
                        }
                    }
                    "isAppInstalled" -> {
                        val packageName = call.argument<String>("package_name")

                        if (packageName == null) {
                            result.error("INVALID_ARGUMENT", "Package name is required", null)
                            return@launch
                        }

                        val isInstalled = appsService.isAppInstalled(packageName)
                        result.success(isInstalled)
                    }
                    "openApp" -> {
                        val packageName = call.argument<String>("package_name")
                        val activityName = call.argument<String>("activity_name")

                        if (packageName == null) {
                            result.error("INVALID_ARGUMENT", "Package name is required", null)
                            return@launch
                        }

                        val launched = appsService.launchApp(packageName, activityName)
                        result.success(launched)
                    }
                    "uninstallApp" -> {
                        val packageName = call.argument<String>("package_name")

                        if (packageName == null) {
                            result.error("INVALID_ARGUMENT", "Package name is required", null)
                            return@launch
                        }

                        val uninstalled = appsService.uninstallApp(packageName)
                        result.success(uninstalled)
                    }
                    "openAppSettings" -> {
                        val packageName = call.argument<String>("package_name")

                        if (packageName == null) {
                            result.error("INVALID_ARGUMENT", "Package name is required", null)
                            return@launch
                        }

                        val opened = appsService.openAppSettings(packageName)
                        result.success(opened)
                    }
                    else -> result.notImplemented()
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                result.error("UNEXPECTED_ERROR", e.message, null)
            }
        }
    }
}
