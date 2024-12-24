import 'dart:async';

import 'package:flutter/services.dart';
import 'package:apps_handler/src/models/app_event.dart';
import 'package:apps_handler/src/models/app_info.dart';

class AppsHandler {
  static const MethodChannel _channel = MethodChannel('apps_handler');
  static const EventChannel _eventChannel =
      EventChannel('apps_handler/apps_channel');

  /// Returns a list of installed applications on the device
  ///
  /// [includeSystemApps] - Whether to include system applications
  /// [includeAppIcons] - Whether to include application icons
  /// [onlyAppsWithLaunchIntent] - Whether to only include apps that can be launched
  static Future<List<AppInfo>> getInstalledApps({
    bool includeSystemApps = false,
    bool includeAppIcons = false,
    bool onlyAppsWithLaunchIntent = false,
  }) async {
    final List<dynamic> apps = await _channel.invokeMethod('getInstalledApps', {
      'include_system_apps': includeSystemApps,
      'include_app_icons': includeAppIcons,
      'only_apps_with_launch_intent': onlyAppsWithLaunchIntent,
    });

    return apps
        .map((app) => AppInfo.fromMap(app as Map<dynamic, dynamic>))
        .toList();
  }

  /// Returns information about a specific app by package name
  ///
  /// [packageName] - The package name of the application
  /// [includeAppIcon] - Whether to include the application icon
  static Future<AppInfo?> getApp(
    String packageName, {
    bool includeAppIcon = false,
  }) async {
    try {
      final Map<dynamic, dynamic>? app = await _channel.invokeMethod('getApp', {
        'package_name': packageName,
        'include_app_icon': includeAppIcon,
      });

      return app != null ? AppInfo.fromMap(app) : null;
    } on PlatformException catch (e) {
      if (e.code == 'APP_NOT_FOUND') {
        return null;
      }
      rethrow;
    }
  }

  /// Checks if an app is installed by package name
  static Future<bool> isAppInstalled(String packageName) async {
    return await _channel.invokeMethod('isAppInstalled', {
      'package_name': packageName,
    });
  }

  /// Opens an installed application by package name
  ///
  /// Returns true if the app was launched successfully
  static Future<bool> openApp(String packageName) async {
    return await _channel.invokeMethod('openApp', {
      'package_name': packageName,
    });
  }

  /// Opens the system settings page for an application
  ///
  /// [packageName] - The package name of the application
  /// Returns true if the settings page was opened successfully
  static Future<bool> openAppSettings(String packageName) async {
    return await _channel.invokeMethod('openAppSettings', {
      'package_name': packageName,
    });
  }

  /// Uninstalls an application by package name
  ///
  /// Returns true if the uninstall process was initiated successfully
  /// Note: This will show the system uninstall dialog to the user
  static Future<bool> uninstallApp(String packageName) async {
    return await _channel.invokeMethod('uninstallApp', {
      'package_name': packageName,
    });
  }

  /// Stream of app install/uninstall events
  static Stream<AppEvent> get appChanges {
    return _eventChannel.receiveBroadcastStream().map(
        (dynamic event) => AppEvent.fromMap(event as Map<dynamic, dynamic>));
  }
}
