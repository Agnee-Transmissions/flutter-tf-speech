package com.agneetransmissions.tf_speech

import android.content.Context

fun readAssetLines(context: Context, labelsFile: String): List<String> {
    return context.assets.open(labelsFile).use { input ->
        input.reader().use { reader ->
            reader.readLines()
        }
    }
}

fun circularArrayCopy(src: FloatArray, n: Int, dest: FloatArray) {
    if (n < dest.size) {
        arrayShiftLeft(dest, n)
        System.arraycopy(src, 0, dest, dest.size - n, n)
    } else {
        System.arraycopy(src, n - dest.size, dest, 0, dest.size)
    }
}

fun arrayShiftLeft(arr: FloatArray, n: Int) {
    System.arraycopy(arr, n, arr, 0, arr.size - n)
}
