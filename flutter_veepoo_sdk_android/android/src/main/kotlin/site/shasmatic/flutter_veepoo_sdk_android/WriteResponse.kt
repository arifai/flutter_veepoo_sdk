package site.shasmatic.flutter_veepoo_sdk_android

import android.util.Log
import com.inuker.bluetooth.library.Code.REQUEST_SUCCESS
import com.veepoo.protocol.listener.base.IBleWriteResponse

class WriteResponse : IBleWriteResponse {
    override fun onResponse(status: Int) {
        if (status != REQUEST_SUCCESS) {
            Log.d("WriteResponse", "Write response: $status")
        }
    }
}