# Flutter TensorFlow Speech

[![pub package](https://img.shields.io/pub/v/flutter_cognito_plugin.svg?style=for-the-badge)](https://pub.dartlang.org/packages/tf_speech)

The TensorFlow [audio recognition tutorial](https://github.com/tensorflow/docs/blob/master/site/en/r1/tutorials/sequences/audio_recognition.md), for use in flutter, with an API that you'll love to use!

```dart
var speech = TfSpeech();
await for (var result in speech.stream) {
  print(result);
}
```

---

<a href="http://www.youtube.com/watch?feature=player_embedded&v=USiOuBkVEIs" target="_blank"><img src="http://img.youtube.com/vi/USiOuBkVEIs/0.jpg" alt="Example app video" width="240" height="180" border="10"/></a>

[Source code](example/lib/main.dart).

---

How does this work? 

We use Android's `AudioRecord` API to record audio in the smallest possible chunks.

These chunks are loaded into a ring buffer.

The ring buffer is periodically fed into the TensorFlow model for inference.

The raw output from the model is passed straight to dart, 
which allows for a great degree of control from dart code.

---
