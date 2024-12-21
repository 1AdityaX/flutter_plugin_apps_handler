import 'package:flutter/material.dart';
import 'package:apps_handler/apps_handler.dart';
import 'dart:typed_data';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Apps Handler Demo',
      theme: ThemeData(
        primarySwatch: Colors.blue,
        useMaterial3: true,
      ),
      home: const MyHomePage(),
    );
  }
}

class MyHomePage extends StatefulWidget {
  const MyHomePage({super.key});

  @override
  State<MyHomePage> createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  List<AppInfo> _apps = [];
  bool _isLoading = false;
  String _lastEvent = 'No events yet';

  @override
  void initState() {
    super.initState();
    _listenToAppChanges();
    _loadApps();
  }

  void _listenToAppChanges() {
    AppsHandler.appChanges.listen((event) {
      setState(() {
        _lastEvent = 'App ${event.packageName} was ${event.event}';
      });
      // Reload apps list when there's a change
      _loadApps();
    });
  }

  Future<void> _loadApps() async {
    setState(() {
      _isLoading = true;
    });

    try {
      final apps = await AppsHandler.getInstalledApps(
        includeSystemApps: true,
        includeAppIcons: true,
        onlyAppsWithLaunchIntent: true,
      );

      if (!mounted) return;
      setState(() {
        _apps = apps;
      });
    } catch (e) {
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Error loading apps: $e')),
      );
    } finally {
      if (mounted) {
        setState(() {
          _isLoading = false;
        });
      }
    }
  }

  Future<void> _openApp(String packageName) async {
    try {
      final launched = await AppsHandler.openApp(packageName);
      if (!launched && mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Failed to open app')),
        );
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Error opening app: $e')),
        );
      }
    }
  }

  Future<void> _uninstallApp(String packageName) async {
    try {
      final result = await AppsHandler.uninstallApp(packageName);
      if (!result && mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Failed to initiate uninstall')),
        );
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Error uninstalling app: $e')),
        );
      }
    }
  }

  Widget _buildAppIcon(List<int>? iconBytes) {
    if (iconBytes == null || iconBytes.isEmpty) {
      return const Icon(Icons.android, size: 40);
    }
    return Image.memory(
      Uint8List.fromList(iconBytes),
      width: 40,
      height: 40,
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Apps Handler'),
        actions: [
          IconButton(
            icon: const Icon(Icons.refresh),
            onPressed: _loadApps,
          ),
        ],
      ),
      body: Column(
        children: [
          // Last event display
          Container(
            padding: const EdgeInsets.all(8.0),
            color: Colors.grey[200],
            width: double.infinity,
            child: Text(
              _lastEvent,
              style: Theme.of(context).textTheme.bodyMedium,
            ),
          ),
          // Apps list
          Expanded(
            child: _isLoading
                ? const Center(child: CircularProgressIndicator())
                : ListView.builder(
                    itemCount: _apps.length,
                    itemBuilder: (context, index) {
                      final app = _apps[index];
                      return ListTile(
                        leading: _buildAppIcon(app.appIcon),
                        title: Text(app.appName),
                        subtitle: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text(app.packageName),
                            Text(
                                'Version: ${app.versionName ?? 'Unknown'} (${app.versionCode})'),
                            Text('Category: ${app.category}'),
                          ],
                        ),
                        isThreeLine: true,
                        trailing: Row(
                          mainAxisSize: MainAxisSize.min,
                          children: [
                            IconButton(
                              icon: const Icon(Icons.delete),
                              onPressed: () {
                                showDialog(
                                  context: context,
                                  builder: (context) => AlertDialog(
                                    title: Text('Uninstall ${app.appName}?'),
                                    content: Text(
                                        'Are you sure you want to uninstall ${app.appName}?'),
                                    actions: [
                                      TextButton(
                                        onPressed: () => Navigator.pop(context),
                                        child: const Text('Cancel'),
                                      ),
                                      TextButton(
                                        onPressed: () {
                                          Navigator.pop(context);
                                          _uninstallApp(app.packageName);
                                        },
                                        child: const Text('Uninstall'),
                                      ),
                                    ],
                                  ),
                                );
                              },
                            ),
                            IconButton(
                              icon: const Icon(Icons.launch),
                              onPressed: () => _openApp(app.packageName),
                            ),
                          ],
                        ),
                        onTap: () {
                          showDialog(
                            context: context,
                            builder: (context) => AlertDialog(
                              title: Text(app.appName),
                              content: SingleChildScrollView(
                                child: Column(
                                  crossAxisAlignment: CrossAxisAlignment.start,
                                  mainAxisSize: MainAxisSize.min,
                                  children: [
                                    Text('Package: ${app.packageName}'),
                                    Text(
                                        'Version: ${app.versionName ?? 'Unknown'} (${app.versionCode})'),
                                    Text('Category: ${app.category}'),
                                    Text('System App: ${app.systemApp}'),
                                    Text('Enabled: ${app.enabled}'),
                                    Text('Data Dir: ${app.dataDir}'),
                                    Text(
                                        'Installer: ${app.installerPackageName ?? 'Unknown'}'),
                                    Text(
                                        'Install Time: ${DateTime.fromMillisecondsSinceEpoch(app.installTime)}'),
                                    Text(
                                        'Update Time: ${DateTime.fromMillisecondsSinceEpoch(app.updateTime)}'),
                                  ],
                                ),
                              ),
                              actions: [
                                TextButton(
                                  onPressed: () => Navigator.pop(context),
                                  child: const Text('Close'),
                                ),
                                TextButton(
                                  onPressed: () {
                                    Navigator.pop(context);
                                    _openApp(app.packageName);
                                  },
                                  child: const Text('Launch'),
                                ),
                              ],
                            ),
                          );
                        },
                      );
                    },
                  ),
          ),
        ],
      ),
    );
  }
}
