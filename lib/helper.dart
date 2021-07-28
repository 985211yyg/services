import 'dart:async';
import 'dart:io';

import 'package:flutter_tts/flutter_tts.dart';
import 'package:volume_control/volume_control.dart';

/// Singleton tool class for tts

/// Use TtsHelper step:
///
/// Method : #isLanguageAvailable judge language, here language is _languageMap's values like "en-US",instead of the type of 'en' etc..
///
/// Method : #getTtsLanguage  help you convert "en" to "en-US".
///
/// Method : #setLanguage help you set Language , but "en-US" is default value
///
/// use example:
/// TtsHelper.instance.speak("speech content");
/// or
/// TtsHelper.instance.setLanguageAndSpeak("speech content", "en-US");
/// ...
class TtsHelper {
  // Locale to tss language map
  static final Map<String, String> _languageMap = {
    'en': "en-US",
    'zh': "zh-CN",
    "ar": "ar-SA",
    "cs": "cs-CZ",
    "da": "da-DK",
    "de": "de-DE",
    "el": "el-GR",
    "es": "es-ES",
    "fi": "fi-FI",
    "fr": "fr-CA",
    "he": "he-IL",
    "hi": "hi-IN",
    "hu": "hu-HU",
    "id": "id-ID",
    "it": "it-IT",
    "ja": "ja-JP",
    "ko": "ko-KR",
    "nl": "nl-BE",
    "no": "no-NO",
    "pl": "pl-PL",
    "pt": "pt-BR",
    "ro": "ro-RO",
    "ru": "ru-RU",
    "sk": "sk-SK",
    "sv": "sv-SE",
    "th": "th-TH",
    "tr": "tr-TR",
    'en-US': "en-US",
    'zh-CN': "zh-CN",
    "ar-SA": "ar-SA",
    "cs-CZ": "cs-CZ",
    "da-DK": "da-DK",
    "de-DE": "de-DE",
    "el-GR": "el-GR",
    "es-ES": "es-ES",
    "fi-FI": "fi-FI",
    "fr-CA": "fr-CA",
    "he-IL": "he-IL",
    "hi-IN": "hi-IN",
    "hu-HU": "hu-HU",
    "id-ID": "id-ID",
    "it-IT": "it-IT",
    "ja-JP": "ja-JP",
    "ko-KR": "ko-KR",
    "nl-BE": "nl-BE",
    "no-NO": "no-NO",
    "pl-PL": "pl-PL",
    "pt-BR": "pt-BR",
    "ro-RO": "ro-RO",
    "ru-RU": "ru-RU",
    "sk-SK": "sk-SK",
    "sv-SE": "sv-SE",
    "th-TH": "th-TH",
    "tr-TR": "tr-TR",
  };

  static bool isPlay = false;

  static const String _defaultL = "zh-CN";
  List<String> _languages;
  final flutterTts = FlutterTts();
  static TtsHelper _instance;

  static TtsHelper get instance => _getInstance();

  factory TtsHelper() => _getInstance();

  static TtsHelper _getInstance() {
    if (_instance == null) {
      _instance = new TtsHelper._internal();
    }
    return _instance;
  }

  TtsHelper._internal() {
    // Initialize
    _initPlatformState();
    flutterTts.setSpeechRate(0.5);
    flutterTts.setVolume(1.0);
    flutterTts.setPitch(1.0);
  }

  void _initPlatformState([String language = _defaultL]) async {
    String defaultL = _getTtsLanguage(language);
    _setLanguage(defaultL);
    if (Platform.isIOS) {
//      await flutterTts.setSharedInstance(true);
      await flutterTts
          .setIosAudioCategory(IosTextToSpeechAudioCategory.playback, [
        IosTextToSpeechAudioCategoryOptions.duckOthers,
        IosTextToSpeechAudioCategoryOptions.allowAirPlay,
        IosTextToSpeechAudioCategoryOptions.allowBluetooth,
        IosTextToSpeechAudioCategoryOptions.allowBluetoothA2DP,
        IosTextToSpeechAudioCategoryOptions.mixWithOthers
      ]);
    }
  }

  String _getTtsLanguage(String localeStr) {
    if (localeStr == null ||
        localeStr.isEmpty ||
        !_languageMap.containsKey(localeStr)) {
      return _defaultL;
    }
    return _languageMap[localeStr];
  }

  // Return whether the result if set language is successful
  Future<void> _setLanguage(String lang) async {
//    String language = _getTtsLanguage(lang);
//    if (language == null || language.isEmpty) {
//      language = _defaultL;
//    }
//    if (Platform.isIOS && !_languages.contains(language)) {
//      return false;
//    }
    final isSet = await flutterTts.setLanguage(lang);
//    return isSet;
  }

  // Returns whether the supported language is supported
//  Future<bool> _isLanguageAvailable(String language) async {
//    final bool isSupport = await flutterTts.isLanguageAvailable(language);
//    return isSupport;
//  }

  Future<void> stopSpeaker() async {
    flutterTts.stop();
  }

  Future<double> setVolume() async {
    double _val = await VolumeControl.volume;
    print("当前音量为$_val}");
    if (_val < 0.4) {
      print("声音过小，调大音量");
      VolumeControl.setVolume(0.8);
    }
    return _val;
  }

  Future<double> speak(String text) async {
    Future<double> futureVolume = setVolume();
    if (isPlay) {
      flutterTts.stop();
      isPlay = false;
//      return;
    }
    var result = await flutterTts.speak(text);
    if (result == 1) {
      isPlay = true;
    } else {
      isPlay = false;
    }
    return futureVolume;
  }
}
