package yukams.app.background_locator_2.pluggables

import android.content.Context
import android.os.Handler
import android.util.Log
import io.flutter.plugin.common.MethodChannel
import yukams.app.background_locator_2.IsolateHolderService
import yukams.app.background_locator_2.Keys
import yukams.app.background_locator_2.PreferencesManager

class InitPluggable : Pluggable {
    private var isInitCallbackCalled = false

    override fun setCallback(context: Context, callbackHandle: Long) {
        Log.i("BackgroundLocatorPlugin/InitPluggable", "setCallback ${callbackHandle}")
        PreferencesManager.setCallbackHandle(context, Keys.INIT_CALLBACK_HANDLE_KEY, callbackHandle)

    }

    override fun onServiceStart(context: Context) {
        Log.i("BackgroundLocatorPlugin/InitPluggable", "onServiceStart")
        if (!isInitCallbackCalled) {
            Log.i("BackgroundLocatorPlugin/InitPluggable", "onServiceStart attempt")
            (PreferencesManager.getCallbackHandle(context, Keys.INIT_CALLBACK_HANDLE_KEY))?.let { initCallback ->
                Log.i("BackgroundLocatorPlugin/InitPluggable", "onServiceStart initCallback ${initCallback}")
                IsolateHolderService.getBinaryMessenger(context)?.let { binaryMessenger ->
                    Log.i("BackgroundLocatorPlugin/InitPluggable", "onServiceStart binaryMessenger")
                    val initialDataMap = PreferencesManager.getDataCallback(context, Keys.INIT_DATA_CALLBACK_KEY)
                    val backgroundChannel = MethodChannel(binaryMessenger, Keys.BACKGROUND_CHANNEL_ID)
                    Handler(context.mainLooper)
                        .post {
                            Log.i("BackgroundLocatorPlugin/InitPluggable", "onServiceStart backgroundChannel.invokeMethod")
                            backgroundChannel.invokeMethod(
                                Keys.BCM_INIT,
                                hashMapOf(
                                    Keys.ARG_INIT_CALLBACK to initCallback,
                                    Keys.ARG_INIT_DATA_CALLBACK to initialDataMap
                                )
                            )
                        }
                }
            }
            isInitCallbackCalled = true
        }
    }

    override fun onServiceDispose(context: Context) {
        Log.i("BackgroundLocatorPlugin/InitPluggable", "onServiceDispose")
        isInitCallbackCalled = false
    }

    fun setInitData(context: Context, data: Map<*, *>) {
        Log.i("BackgroundLocatorPlugin/InitPluggable", "setInitData")
        PreferencesManager.setDataCallback(context, Keys.INIT_DATA_CALLBACK_KEY, data)
    }
}