
import 'flutter_veepoo_sdk_android_platform_interface.dart';

class FlutterVeepooSdkAndroid {
  Future<String?> getPlatformVersion() {
    return FlutterVeepooSdkAndroidPlatform.instance.getPlatformVersion();
  }
}
