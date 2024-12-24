# Apps Handler Plugin

A Flutter plugin to get information about installed applications on Android devices, monitor app installations/uninstallations, and launch applications.

## Features

- Get list of installed applications
- Get detailed application information
- Monitor app install/uninstall events in real-time
- Launch applications by package name
- Check if specific apps are installed
- Open app system settings
- Support for app icons
- Detailed app metadata (version, category, install time, etc.)
- Uninstall applications

## Getting Started

### Installation

Add this to your package's `pubspec.yaml` file:

```yaml
dependencies:
  apps_handler: ^latest_version
```

### Platform Support

| Android | iOS | MacOS | Web | Linux | Windows |
|---------|-----|-------|-----|--------|---------|
| ✅      | ❌   | ❌    | ❌  | ❌     | ❌      |

### Required Permissions

Add these permissions to your Android Manifest (`android/app/src/main/AndroidManifest.xml`):

```xml
<uses-permission android:name="android.permission.QUERY_ALL_PACKAGES" />
<uses-permission android:name="android.permission.REQUEST_DELETE_PACKAGES" />
```

Note: For Android 11 (API level 30) and above, you need to add queries to your manifest:

```xml
<queries>
    <intent>
        <action android:name="android.intent.action.MAIN" />
    </intent>
</queries>
```

## Usage

### Import the package

```dart
import 'package:apps_handler/apps_handler.dart';
```

### Get Installed Applications

```dart
// Get all installed apps
Future<void> getInstalledApps() async {
  final apps = await AppsHandler.getInstalledApps(
    includeSystemApps: false,
    includeAppIcons: true,
    onlyAppsWithLaunchIntent: true,
  );
  
  for (final app in apps) {
    print('App Name: ${app.appName}');
    print('Package Name: ${app.packageName}');
    print('Version: ${app.versionName}');
    print('Category: ${app.category}');
  }
}
```

### Get Specific App Information

```dart
Future<void> getAppInfo(String packageName) async {
  final app = await AppsHandler.getApp(
    packageName,
    includeAppIcon: true,
  );
  
  if (app != null) {
    print('App Name: ${app.appName}');
    print('Install Time: ${DateTime.fromMillisecondsSinceEpoch(app.installTime)}');
    print('Update Time: ${DateTime.fromMillisecondsSinceEpoch(app.updateTime)}');
  }
}
```

### Check if App is Installed

```dart
Future<void> checkAppInstallation(String packageName) async {
  final isInstalled = await AppsHandler.isAppInstalled(packageName);
  print('Is app installed: $isInstalled');
}
```

### Launch an Application

```dart
Future<void> launchApp(String packageName) async {
  final launched = await AppsHandler.openApp(packageName);
  print('App launch ${launched ? 'successful' : 'failed'}');
}
```

### Open App Settings

```dart
Future<void> openSettings(String packageName) async {
  final opened = await AppsHandler.openAppSettings(packageName);
  print('Settings opened: $opened');
}
```

### Uninstall an Application

```dart
Future<void> uninstallApp(String packageName) async {
  final uninstalled = await AppsHandler.uninstallApp(packageName);
  print('Uninstall initiated: $uninstalled');
}
```

### Monitor App Changes

```dart
void monitorAppChanges() {
  AppsHandler.appChanges.listen((event) {
    switch (event.event) {
      case AppEventType.installed:
        print('App installed: ${event.packageName}');
        break;
      case AppEventType.uninstalled:
        print('App uninstalled: ${event.packageName}');
        break;
      case AppEventType.updated:
        print('App updated: ${event.packageName}');
        break;
    }
  });
}
```

## API Reference

### AppsHandler

#### Methods

##### `getInstalledApps`
```dart
static Future<List<AppInfo>> getInstalledApps({
  bool includeSystemApps = false,
  bool includeAppIcons = false,
  bool onlyAppsWithLaunchIntent = false,
})
```

##### `getApp`
```dart
static Future<AppInfo?> getApp(
  String packageName, {
  bool includeAppIcon = false,
})
```

##### `isAppInstalled`
```dart
static Future<bool> isAppInstalled(String packageName)
```

##### `openApp`
```dart
static Future<bool> openApp(String packageName)
```

##### `openAppSettings`
```dart
static Future<bool> openAppSettings(String packageName)
```

##### `uninstallApp`
```dart
static Future<bool> uninstallApp(String packageName)
```

##### `appChanges`
```dart
static Stream<AppEvent> get appChanges
```

### AppInfo Class

```dart
class AppInfo {
  final String appName;
  final String packageName;
  final String category;
  final String? versionName;
  final int versionCode;
  final String dataDir;
  final bool systemApp;
  final String? installerPackageName;
  final bool enabled;
  final int installTime;
  final int updateTime;
  final List<int>? appIcon;
}
```

### AppEvent Class

```dart
class AppEvent {
  final String packageName;
  final AppEventType event;
}

enum AppEventType {
  installed,
  uninstalled,
  updated,
}
```

## Complete Example

```dart
import 'package:flutter/material.dart';
import 'package:apps_handler/apps_handler.dart';

void main() {
  runApp(MaterialApp(
    home: Scaffold(
      appBar: AppBar(title: Text('Apps Handler Example')),
      body: AppsHandlerDemo(),
    ),
  ));
}

class AppsHandlerDemo extends StatefulWidget {
  @override
  _AppsHandlerDemoState createState() => _AppsHandlerDemoState();
}

class _AppsHandlerDemoState extends State<AppsHandlerDemo> {
  List<AppInfo> _apps = [];

  @override
  void initState() {
    super.initState();
    _loadApps();
    _setupAppChangeListener();
  }

  Future<void> _loadApps() async {
    final apps = await AppsHandler.getInstalledApps(
      includeAppIcons: true,
      onlyAppsWithLaunchIntent: true,
    );
    setState(() => _apps = apps);
  }

  void _setupAppChangeListener() {
    AppsHandler.appChanges.listen((event) {
      print('App ${event.packageName} was ${event.event}');
      _loadApps(); // Refresh list when apps change
    });
  }

  @override
  Widget build(BuildContext context) {
    return ListView.builder(
      itemCount: _apps.length,
      itemBuilder: (context, index) {
        final app = _apps[index];
        return ListTile(
          leading: app.appIcon != null 
            ? Image.memory(Uint8List.fromList(app.appIcon!))
            : Icon(Icons.android),
          title: Text(app.appName),
          subtitle: Text(app.packageName),
          onTap: () => AppsHandler.openApp(app.packageName),
          onLongPress: () => AppsHandler.openAppSettings(app.packageName),
        );
      },
    );
  }
}
```

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Credits

This project is inspired by and builds upon the work of:
- [installed_apps](https://github.com/sharmadhiraj/installed_apps)
- [flutter_plugin_device_apps](https://github.com/g123k/flutter_plugin_device_apps)