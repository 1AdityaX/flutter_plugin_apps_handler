package dev.aditya.apps_handler

import android.content.Context
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppsHandlerPlugin : FlutterPlugin, MethodCallHandler {
    private lateinit var context: Context
    private lateinit var methodChannel: MethodChannel
    private lateinit var eventChannel: EventChannel
    private lateinit var appsService: AppsService
    private lateinit var applicationChangeListener: ApplicationChangeListener
    private val scope = CoroutineScope(Dispatchers.Main)

    override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
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
        eventChannel.setStreamHandler(null)
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
                        withContext(Dispatchers.Main) {
                            result.success(apps)
                        }
                    }
                    "getApp" -> {
                        val packageName = call.argument<String>("package_name")
                        val includeAppIcon = call.argument<Boolean>("include_app_icon") ?: false

                        if (packageName == null) {
                            withContext(Dispatchers.Main) {
                                result.error("INVALID_ARGUMENT", "Package name is required", null)
                            }
                            return@launch
                        }

                        val app = appsService.getApp(packageName, includeAppIcon)
                        withContext(Dispatchers.Main) {
                            if (app != null) {
                                result.success(app)
                            } else {
                                result.error("APP_NOT_FOUND", "App not found", null)
                            }
                        }
                    }
                    "isAppInstalled" -> {
                        val packageName = call.argument<String>("package_name")
                        
                        if (packageName == null) {
                            withContext(Dispatchers.Main) {
                                result.error("INVALID_ARGUMENT", "Package name is required", null)
                            }
                            return@launch
                        }

                        val isInstalled = appsService.isAppInstalled(packageName)
                        withContext(Dispatchers.Main) {
                            result.success(isInstalled)
                        }
                    }
                    "openApp" -> {
                        val packageName = call.argument<String>("package_name")
                        
                        if (packageName == null) {
                            withContext(Dispatchers.Main) {
                                result.error("INVALID_ARGUMENT", "Package name is required", null)
                            }
                            return@launch
                        }

                        val launched = appsService.launchApp(packageName)
                        withContext(Dispatchers.Main) {
                            result.success(launched)
                        }
                    }
                    "uninstallApp" -> {
                        val packageName = call.argument<String>("package_name")
                        
                        if (packageName == null) {
                            withContext(Dispatchers.Main) {
                                result.error("INVALID_ARGUMENT", "Package name is required", null)
                            }
                            return@launch
                        }

                        val uninstalled = appsService.uninstallApp(packageName)
                        withContext(Dispatchers.Main) {
                            result.success(uninstalled)
                        }
                    }
                    
                    else -> {
                        withContext(Dispatchers.Main) {
                            result.notImplemented()
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    result.error("UNEXPECTED_ERROR", e.message, null)
                }
            }
        }
    }
}
