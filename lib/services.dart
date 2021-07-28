import 'dart:async';
import 'dart:io';

import 'package:flutter/services.dart';

import './helper.dart';

typedef EventHandler(String event);

class Services {
  static MethodChannel _channel;
  EventHandler _eventHandler;
  static TtsHelper ServiceTTs;

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  // 工厂模式
  factory Services() => _getInstance();

  static Services get instance => _getInstance();
  static Services _instance;

  Services._internal() {
    if (Platform.isIOS) {
      ServiceTTs = TtsHelper();
    }
    // 初始化
    _channel = MethodChannel('com.reemii.ServicesPlugin');
  }

  static Services _getInstance() {
    if (_instance == null) {
      _instance = new Services._internal();
    }
    return _instance;
  }

  void addEventHandler(EventHandler eventHandler) {
    _eventHandler = eventHandler;
    _channel.setMethodCallHandler(_handleMethod);
  }

  Future<Null> _handleMethod(MethodCall call) async {
    switch (call.method) {
      case "backgroundBeep":
        _eventHandler("backgroundBeep");
        break;
      default:
        throw new UnsupportedError("Unrecognized Event");
    }
  }

  Future init(String token) async {
    await _channel.invokeMethod('init', token);
    return Future.value();
  }

  Future speaker(String text) async {
    if (Platform.isAndroid) return _channel.invokeMethod('speaker', text);
    return ServiceTTs.speak(text);
  }

  Future stopSpeaker() async {
    if (Platform.isIOS) {
      ServiceTTs.stopSpeaker();
    } else
      await _channel.invokeMethod('stop_speaker');
  }

  Future orderPaid() async {
    await _channel.invokeMethod('order_paid');
    return Future.value();
  }

  Future update(Map<String, dynamic> updateModel) async {
    await _channel.invokeMethod('update', updateModel);
    return Future.value();
  }

  void setDomain(String domain) {
    _channel.invokeMethod("domain", domain);
  }

  void setting() {
    _channel.invokeMethod("setting");
  }

  void exit() {
    _channel.invokeMethod("exit");
  }
}
