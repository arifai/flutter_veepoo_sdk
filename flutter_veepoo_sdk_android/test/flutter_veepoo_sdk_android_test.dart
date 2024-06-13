import 'package:flutter_test/flutter_test.dart';
import 'package:flutter_veepoo_sdk_android/flutter_veepoo_sdk_android.dart';
import 'package:flutter_veepoo_sdk_android/flutter_veepoo_sdk_android_platform_interface.dart';
import 'package:flutter_veepoo_sdk_android/flutter_veepoo_sdk_android_method_channel.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

class MockFlutterVeepooSdkAndroidPlatform
    with MockPlatformInterfaceMixin
    implements FlutterVeepooSdkAndroidPlatform {

  @override
  Future<String?> getPlatformVersion() => Future.value('42');
}

void main() {
  final FlutterVeepooSdkAndroidPlatform initialPlatform = FlutterVeepooSdkAndroidPlatform.instance;

  test('$MethodChannelFlutterVeepooSdkAndroid is the default instance', () {
    expect(initialPlatform, isInstanceOf<MethodChannelFlutterVeepooSdkAndroid>());
  });

  test('getPlatformVersion', () async {
    FlutterVeepooSdkAndroid flutterVeepooSdkAndroidPlugin = FlutterVeepooSdkAndroid();
    MockFlutterVeepooSdkAndroidPlatform fakePlatform = MockFlutterVeepooSdkAndroidPlatform();
    FlutterVeepooSdkAndroidPlatform.instance = fakePlatform;

    expect(await flutterVeepooSdkAndroidPlugin.getPlatformVersion(), '42');
  });
}
