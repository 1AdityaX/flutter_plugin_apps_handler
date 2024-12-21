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

  const AppInfo({
    required this.appName,
    required this.packageName,
    required this.category,
    required this.versionName,
    required this.versionCode,
    required this.dataDir,
    required this.systemApp,
    required this.installerPackageName,
    required this.enabled,
    required this.installTime,
    required this.updateTime,
    this.appIcon,
  });

  factory AppInfo.fromMap(Map<dynamic, dynamic> map) {
    return AppInfo(
      appName: map['app_name'] as String,
      packageName: map['package_name'] as String,
      category: map['category'] as String,
      versionName: map['version_name'] as String?,
      versionCode: map['version_code'] as int,
      dataDir: map['data_dir'] as String,
      systemApp: map['system_app'] as bool,
      installerPackageName: map['installer_package_name'] as String?,
      enabled: map['enabled'] as bool,
      installTime: map['install_time'] as int,
      updateTime: map['update_time'] as int,
      appIcon: map['app_icon'] != null ? List<int>.from(map['app_icon']) : null,
    );
  }

  Map<String, dynamic> toMap() {
    return {
      'app_name': appName,
      'package_name': packageName,
      'category': category,
      'version_name': versionName,
      'version_code': versionCode,
      'data_dir': dataDir,
      'system_app': systemApp,
      'installer_package_name': installerPackageName,
      'enabled': enabled,
      'install_time': installTime,
      'update_time': updateTime,
      'app_icon': appIcon,
    };
  }

  @override
  String toString() {
    return 'AppInfo(appName: $appName, packageName: $packageName, category: $category, '
        'versionName: $versionName, versionCode: $versionCode, dataDir: $dataDir, '
        'systemApp: $systemApp, installerPackageName: $installerPackageName, '
        'enabled: $enabled, installTime: $installTime, updateTime: $updateTime, '
        'hasIcon: ${appIcon != null})';
  }

  @override
  bool operator ==(Object other) {
    if (identical(this, other)) return true;
    return other is AppInfo && other.packageName == packageName;
  }

  @override
  int get hashCode => packageName.hashCode;
}
