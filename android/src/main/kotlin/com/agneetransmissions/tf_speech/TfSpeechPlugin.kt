package com.agneetransmissions.tf_speech

import androidx.annotation.NonNull
import com.pycampers.plugin_scaffold.createPluginScaffold
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.PluginRegistry.Registrar

const val channelName = "com.agneetransmissions.tf_speech"

class TfSpeechPlugin : FlutterPlugin {
    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        val methods = SpeechMethods(flutterPluginBinding.applicationContext) {
            flutterPluginBinding.flutterAssets.getAssetFilePathByName(it)
        }
        createPluginScaffold(flutterPluginBinding.binaryMessenger, channelName, methods)
    }

    // This static function is optional and equivalent to onAttachedToEngine. It supports the old
    // pre-Flutter-1.12 Android projects. You are encouraged to continue supporting
    // plugin registration via this function while apps migrate to use the new Android APIs
    // post-flutter-1.12 via https://flutter.dev/go/android-project-migration.
    //
    // It is encouraged to share logic between onAttachedToEngine and registerWith to keep
    // them functionally equivalent. Only one of onAttachedToEngine or registerWith will be called
    // depending on the user's project. onAttachedToEngine or registerWith must both be defined
    // in the same class.
    companion object {
        @JvmStatic
        fun registerWith(registrar: Registrar) {
            val methods = SpeechMethods(registrar.context()) {
                registrar.lookupKeyForAsset(it)
            }
            createPluginScaffold(registrar.messenger(), channelName, methods)
        }
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {}
}
