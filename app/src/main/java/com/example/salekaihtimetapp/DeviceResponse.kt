package com.example.salekaihtimetapp

import io.ktor.http.HttpStatusCode

/**
 * Object that is returned to ui from client/device network requests.
 * */
class DeviceResponse (var msg: String, var code: HttpStatusCode){

    companion object{
        fun buildBadRequestResponse(): DeviceResponse{
            return DeviceResponse("Err: 420, Malformed request.", HttpStatusCode.BadRequest)
        }

        fun buildTimeoutResponse(): DeviceResponse{
            return DeviceResponse("Err: 69, Cannot connect to device.", HttpStatusCode.RequestTimeout)
        }
    }
}