import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'flutter_veepoo_sdk_android_method_channel.dart';

abstract class FlutterVeepooSdkAndroidPlatform extends PlatformInterface {
  /// Constructs a FlutterVeepooSdkAndroidPlatform.
  FlutterVeepooSdkAndroidPlatform() : super(token: _token);

  static final Object _token = Object();

  static FlutterVeepooSdkAndroidPlatform _instance = MethodChannelFlutterVeepooSdkAndroid();

  /// The default instance of [FlutterVeepooSdkAndroidPlatform] to use.
  ///
  /// Defaults to [MethodChannelFlutterVeepooSdkAndroid].
  static FlutterVeepooSdkAndroidPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [FlutterVeepooSdkAndroidPlatform] when
  /// they register themselves.
  static set instance(FlutterVeepooSdkAndroidPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<String?> getPlatformVersion() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }
}
