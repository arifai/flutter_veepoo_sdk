import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'flutter_veepoo_sdk_android_platform_interface.dart';

/// An implementation of [FlutterVeepooSdkAndroidPlatform] that uses method channels.
class MethodChannelFlutterVeepooSdkAndroid extends FlutterVeepooSdkAndroidPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('flutter_veepoo_sdk_android');

  @override
  Future<String?> getPlatformVersion() async {
    final version = await methodChannel.invokeMethod<String>('getPlatformVersion');
    return version;
  }
}
