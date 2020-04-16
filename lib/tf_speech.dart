import 'dart:async';

import 'package:flutter/services.dart';
import 'package:plugin_scaffold/plugin_scaffold.dart';

class TfSpeech {
  static const channel = MethodChannel('com.agneetransmissions.tf_speech');
  static const assetsRoot = "packages/tf_speech/assets";

  /// path to labels file (flutter asset)
  final String labelsFile;

  /// path to model file (flutter asset)
  final String modelFile;

  /// input sample rate layer name
  final String sampleRateName;

  /// input data layer name
  final String inputDataName;

  /// output layer name
  final String outputScoresName;

  /// sample rate for audio recording, depends on model
  final int sampleRateHz;

  /// length of a sample in milliseconds, depends on model
  final int sampleDurationMs;

  /// minimum delay between model passes in milliseconds.
  /// A higher delay results in lower CPU usage, at the cost of accuracy.
  final int inferenceDelayMs;

  /// Create a new
  TfSpeech({
    this.labelsFile: "$assetsRoot/conv_actions_labels.txt",
    this.modelFile: "$assetsRoot/conv_actions_frozen.pb",
    this.sampleRateName: "decoded_sample_data:1",
    this.inputDataName: "decoded_sample_data:0",
    this.outputScoresName: "labels_softmax",
    this.sampleRateHz: 16000,
    this.sampleDurationMs: 1000,
    this.inferenceDelayMs: 50,
  });

  StreamController _control;

  Stream<Map<String, double>> get stream {
    _control ??= PluginScaffold.createStreamController(
      channel,
      "recognizer",
      {
        "modelFile": modelFile,
        "labelsFile": labelsFile,
        "sampleRateName": sampleRateName,
        "inputDataName": inputDataName,
        "outputScoresName": outputScoresName,
        "inferenceDelayMs": inferenceDelayMs,
        "sampleRateHz": sampleRateHz,
        "sampleDurationMs": sampleDurationMs,
      },
    );
    return _control.stream.map((x) => Map<String, double>.from(x));
  }

  void close() {
    _control?.close();
    _control = null;
  }
}
