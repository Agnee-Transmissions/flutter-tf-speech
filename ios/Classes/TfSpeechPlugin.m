#import "TfSpeechPlugin.h"
#if __has_include(<tf_speech/tf_speech-Swift.h>)
#import <tf_speech/tf_speech-Swift.h>
#else
// Support project import fallback if the generated compatibility header
// is not copied when this plugin is created as a library.
// https://forums.swift.org/t/swift-static-libraries-dont-copy-generated-objective-c-header/19816
#import "tf_speech-Swift.h"
#endif

@implementation TfSpeechPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftTfSpeechPlugin registerWithRegistrar:registrar];
}
@end
