import Flutter
import UIKit

public class SwiftServicesPlugin: NSObject, FlutterPlugin {
  public static func register(with registrar: FlutterPluginRegistrar) {
    let channel = FlutterMethodChannel(name: "com.reemii.ServicesPlugin", binaryMessenger: registrar.messenger())
    let instance = SwiftServicesPlugin()
    registrar.addMethodCallDelegate(instance, channel: channel)
  }

  public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
//    result("iOS " + UIDevice.current.systemVersion)
    switch call.method {
    case "init":
        UserDefaults.standard.set(call.arguments, forKey: "brToken")
        break
    case "domain":
        UserDefaults.standard.set(call.arguments, forKey: "domain")
        break
    default:
        break
    }
  }
}
