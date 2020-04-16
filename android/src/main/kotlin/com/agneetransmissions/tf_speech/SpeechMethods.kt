package com.agneetransmissions.tf_speech

import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import com.pycampers.plugin_scaffold.catchErrors
import io.flutter.plugin.common.EventChannel.EventSink
import org.tensorflow.contrib.android.TensorFlowInferenceInterface
import java.util.concurrent.atomic.AtomicInteger

const val sizeOfFloat = 4

class SpeechMethods(val context: Context, val getAssetPath: ((String) -> String)) {
    var activeRecognizer = AtomicInteger(-1)

    fun recognizerOnListen(id: Int, args: Any?, sink: EventSink) {
        val thread = Thread {
            catchErrors(sink) {
                recognizerThread(id, args as Map<*, *>, sink)
            }
        }
        thread.start()
    }

    fun recognizerThread(id: Int, args: Map<*, *>, sink: EventSink) {
        activeRecognizer.set(id)

        val modelFile = getAssetPath(args["modelFile"] as String)
        val inferenceInterface = TensorFlowInferenceInterface(context.assets, modelFile)

        val labelsFile = getAssetPath(args["labelsFile"] as String)
        val labels = readAssetLines(context, labelsFile)

        // determine buffer sizes
        val sampleRateHz = args["sampleRateHz"] as Int
        val sampleDurationMs = args["sampleDurationMs"] as Int

        val inputSize = sampleRateHz * sampleDurationMs / 1000
        val minBufferBytes = AudioRecord.getMinBufferSize(
            sampleRateHz, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_FLOAT
        )
        if (minBufferBytes == AudioRecord.ERROR || minBufferBytes == AudioRecord.ERROR_BAD_VALUE) {
            sink.error(
                minBufferBytes.toString(),
                "Could not determine AudioRecord minBufferSize.",
                null
            )
            return
        }

        // create buffers
        val recordBuffer = FloatArray(minBufferBytes / sizeOfFloat)
        val inputBuffer = FloatArray(inputSize)

        // create AudioRecord
        val record = AudioRecord(
            MediaRecorder.AudioSource.DEFAULT,
            sampleRateHz,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_FLOAT,
            recordBuffer.size
        )
        if (record.state != AudioRecord.STATE_INITIALIZED) {
            sink.error(record.state.toString(), "Could not initialize AudioRecord", null)
            return
        }

        // start
        record.startRecording()
        try {
            mainLoop(
                id,
                args,
                sink,
                labels,
                sampleRateHz,
                record,
                inferenceInterface,
                recordBuffer,
                inputBuffer
            )
        } finally {
            record.stop()
            record.release()
            inferenceInterface.close()
        }
    }

    fun mainLoop(
        id: Int,
        args: Map<*, *>,
        sink: EventSink,
        labels: List<String>,
        sampleRateHz: Int,
        record: AudioRecord,
        inferenceInterface: TensorFlowInferenceInterface,
        recordBuffer: FloatArray,
        inputBuffer: FloatArray
    ) {
        // model parameters
        val outputScores = FloatArray(labels.size)
        val sampleRateName = args["sampleRateName"] as String
        val inputDataName = args["inputDataName"] as String
        val outputScoresName = args["outputScoresName"] as String
        val inferenceDelayMs = args["inferenceDelayMs"] as Int
        val outputScoresNames = arrayOf(outputScoresName)
        val sampleRates = intArrayOf(sampleRateHz)

        var lastInferenceMs = System.currentTimeMillis()

        while (activeRecognizer.get() == id) {
            // read audio
            val n = record.read(
                recordBuffer,
                0,
                recordBuffer.size,
                AudioRecord.READ_BLOCKING
            )
            // println("read $n frames")

            // copy audio buffer into circular input buffer
            circularArrayCopy(recordBuffer, n, inputBuffer)

            // run inference after every inferenceDelayMs, to save CPU
            if (System.currentTimeMillis() - lastInferenceMs > inferenceDelayMs) {
                // feed input into model
                inferenceInterface.feed(sampleRateName, sampleRates)
                inferenceInterface.feed(
                    inputDataName,
                    inputBuffer,
                    inputBuffer.size.toLong(), 1
                )
                inferenceInterface.run(outputScoresNames)
                inferenceInterface.fetch(outputScoresName, outputScores)

                // send result to dart
                val outputMap = outputScores
                    .mapIndexed { index, x -> Pair(labels[index], x) }
                    .toMap()
                sink.success(outputMap)

                lastInferenceMs = System.currentTimeMillis()
            }
        }
    }

    fun recognizerOnCancel(id: Int, args: Any?) {
        activeRecognizer.compareAndSet(id, -1)
    }
}
