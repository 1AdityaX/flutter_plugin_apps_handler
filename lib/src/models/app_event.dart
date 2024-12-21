enum AppEventType {
  installed,
  uninstalled,
  updated,
}

class AppEvent {
  final String packageName;
  final AppEventType event;

  const AppEvent({
    required this.packageName,
    required this.event,
  });

  factory AppEvent.fromMap(Map<dynamic, dynamic> map) {
    return AppEvent(
      packageName: map['package_name'] as String,
      event: _stringToEventType(map['event'] as String),
    );
  }

  static AppEventType _stringToEventType(String value) {
    switch (value) {
      case 'installed':
        return AppEventType.installed;
      case 'uninstalled':
        return AppEventType.uninstalled;
      case 'updated':
        return AppEventType.updated;
      default:
        throw ArgumentError('Invalid AppEventType: $value');
    }
  }

  String _eventTypeToString(AppEventType type) {
    switch (type) {
      case AppEventType.installed:
        return 'installed';
      case AppEventType.uninstalled:
        return 'uninstalled';
      case AppEventType.updated:
        return 'updated';
    }
  }

  Map<String, dynamic> toMap() {
    return {
      'package_name': packageName,
      'event': _eventTypeToString(event),
    };
  }

  @override
  String toString() =>
      'AppEvent(packageName: $packageName, event: ${_eventTypeToString(event)})';

  @override
  bool operator ==(Object other) {
    if (identical(this, other)) return true;
    return other is AppEvent &&
        other.packageName == packageName &&
        other.event == event;
  }

  @override
  int get hashCode => packageName.hashCode ^ event.hashCode;
}
