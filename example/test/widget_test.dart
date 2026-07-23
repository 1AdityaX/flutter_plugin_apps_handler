import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:apps_handler_example/main.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  testWidgets('shows the app list', (WidgetTester tester) async {
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger
        .setMockMethodCallHandler(
      const MethodChannel('apps_handler'),
      (_) async => <Object?>[],
    );
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger
        .setMockMethodCallHandler(
      const MethodChannel('apps_handler/apps_channel'),
      (_) async => null,
    );

    await tester.pumpWidget(const MyApp());
    await tester.pump();

    expect(find.text('Apps Handler'), findsOneWidget);
  });
}
