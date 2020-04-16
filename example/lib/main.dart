import 'dart:async';

import 'package:flutter/material.dart';
import 'package:permission_handler/permission_handler.dart';
import 'package:tf_speech/tf_speech.dart';

void main() => runApp(MyApp());

class MyApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        body: SafeArea(
          child: Home(),
        ),
      ),
    );
  }
}

class Home extends StatefulWidget {
  @override
  _HomeState createState() => _HomeState();
}

class _HomeState extends State<Home> {
  static const moveDelayMs = 10;
  static const moveDelta = 0.005;

  static const thresholds = {
    "up": 0.5,
    "down": 0.2,
    "left": 0.3,
    "right": 0.3,
    "stop": 0.5,
  };

  @override
  Widget build(BuildContext context) {
    return Stack(
      children: <Widget>[
        if (result.isNotEmpty)
          Positioned.fill(
            child: PredictionBar(
              thresholds: thresholds,
              values: result,
            ),
          ),
        Positioned.fill(
          child: Center(
            child: Transform.translate(
              offset: offset,
              child: Image.asset(
                'assets/rocket.png',
                width: 50,
              ),
            ),
          ),
        ),
      ],
    );
  }

  var speech = TfSpeech();
  var offset = Offset.zero;
  var result = <String, double>{};

  @override
  void initState() {
    super.initState();
    startRecognizer();
  }

  @override
  void dispose() {
    super.dispose();
    speech.close();
  }

  Future<void> startRecognizer() async {
    if (!await Permission.speech.request().isGranted) return;

    Timer timer;

    await for (result in speech.stream) {
      if (!mounted) return;
      setState(() {});

      // select keywords that pass the threshold, and move accordingly
      for (var entry in thresholds.entries) {
        if (result[entry.key] < entry.value) continue;

        switch (entry.key) {
          case "up":
            timer?.cancel();
            timer = createTimer(deltaY: -moveDelta);
            break;
          case "down":
            timer?.cancel();
            timer = createTimer(deltaY: moveDelta);
            break;
          case "right":
            timer?.cancel();
            timer = createTimer(deltaX: moveDelta);
            break;
          case "left":
            timer?.cancel();
            timer = createTimer(deltaX: -moveDelta);
            break;
          case "stop":
            timer?.cancel();
            break;
          default:
            break;
        }
        break;
      }
    }
  }

  Timer createTimer({double deltaX: 0, double deltaY: 0}) {
    return Timer.periodic(Duration(milliseconds: moveDelayMs), (_) {
      var size = MediaQuery.of(context).size;

      var dx = wrapValue(offset.dx + size.width * deltaX, size.width / 2);
      var dy = wrapValue(offset.dy + size.height * deltaY, size.height / 2);

      setState(() {
        offset = Offset(dx, dy);
      });
    });
  }
}

double wrapValue(double dx, double max) {
  if (dx < -max) {
    dx = max;
  } else if (dx > max) {
    dx = -max;
  }
  return dx;
}

class PredictionBar extends StatelessWidget {
  final Map<String, double> values;
  final double height, width;
  final Map<String, double> thresholds;

  const PredictionBar({
    Key key,
    @required this.values,
    @required this.thresholds,
    this.height = 80,
    this.width = 10,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    var theme = Theme.of(context);
    return Center(
      child: SingleChildScrollView(
        scrollDirection: Axis.horizontal,
        child: Row(
          mainAxisAlignment: MainAxisAlignment.center,
          children: <Widget>[
            for (var entry in values.entries)
              if (thresholds.containsKey(entry.key))
                Padding(
                  padding: EdgeInsets.all(5),
                  child: Column(
                    children: <Widget>[
                      Container(
                        height: height,
                        width: width,
                        decoration: BoxDecoration(
                          borderRadius: BorderRadius.circular(10),
                          border: Border.all(color: theme.dividerColor),
                        ),
                        alignment: Alignment.bottomLeft,
                        child: Container(
                          decoration: BoxDecoration(
                            borderRadius: BorderRadius.circular(10),
                            color: entry.value < thresholds[entry.key]
                                ? theme.disabledColor
                                : theme.primaryColor,
                          ),
                          height: entry.value * height,
                        ),
                      ),
                      SizedBox(height: 10),
                      RotatedBox(
                        quarterTurns: 3,
                        child: Text(
                          entry.key,
                          style: TextStyle(fontSize: 18),
                        ),
                      ),
                    ],
                  ),
                )
          ],
        ),
      ),
    );
  }
}
