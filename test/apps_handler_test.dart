import 'package:apps_handler/apps_handler.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';

const _appMap = {
  'app_name': 'Example',
  'package_name': 'com.example',
  'activity_name': 'com.example.MainActivity',
  'category': 'Other',
  'version_name': '1.0',
  'version_code': 1,
  'data_dir': '/data/user/0/com.example',
  'system_app': false,
  'installer_package_name': null,
  'enabled': true,
  'install_time': 0,
  'update_time': 0,
  'app_icon': null,
};

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  const channel = MethodChannel('apps_handler');
  final calls = <MethodCall>[];

  setUp(() {
    calls.clear();
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger
        .setMockMethodCallHandler(channel, (call) async {
      calls.add(call);
      return true;
    });
  });

  tearDown(() {
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger
        .setMockMethodCallHandler(channel, null);
  });

  test('launches a resolved activity', () async {
    await AppsHandler.openApp(AppInfo.fromMap(_appMap));

    expect(calls.single.method, 'openApp');
    expect(calls.single.arguments, {
      'package_name': 'com.example',
      'activity_name': 'com.example.MainActivity',
    });
  });

  test('reads the resolved launcher activity', () {
    final app = AppInfo.fromMap(_appMap);

    expect(app.activityName, 'com.example.MainActivity');
  });
}
