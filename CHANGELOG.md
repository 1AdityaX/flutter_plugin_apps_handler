# Changelog

## 2.0.0 - 2026-07-24

- **Breaking:** `openApp` now accepts an `AppInfo`
- Launch apps by resolved activity component
- Update build dependencies

## 1.0.1 - 2024-12-24

### Added
- New `openAppSettings` method to open system settings page for specific apps
- Added long press action in example app to demonstrate settings access
- Updated documentation with new feature and examples

### Fixed
- Improved error handling in native code
- Enhanced type safety in Dart implementation

## 1.0.0 - 2024-12-21

### Initial Release 🎉

#### Features
- **App Discovery**
  - Get a list of installed applications
  - Filter system apps and apps with launch intent
  - Retrieve app icons and detailed app information

- **Real-time Monitoring**
  - Track app installations, uninstallations, and updates via event streams

- **App Management**
  - Launch applications by package name
  - Check if specific apps are installed
  - Uninstall applications using the system dialog

- **Detailed App Information**
  - App name, package name, version details, category, and system app status
  - Installation and update timestamps
  - Application icon (optional)

#### Platform Support
- ✅ Android (Minimum SDK 21)
- ❌ iOS (not supported)
