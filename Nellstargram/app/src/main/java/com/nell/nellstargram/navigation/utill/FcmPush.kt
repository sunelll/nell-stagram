package com.nell.nellstargram.navigation.utill

import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.nell.nellstargram.navigation.model.PushDTO
import okhttp3.*
import java.io.IOException

class FcmPush {

   var JSON = MediaType.parse("application/json; charset=utf-8; ")
   var url = "https://fcm.googleapis.com/fcm/send"
   var serverKey = "AIzaSyCp1q4x_Iyfk2CqEAGgkabXcmV55KoOaao"
   var gson : Gson? = null
   var okhttpClient : OkHttpClient? = null

    companion object{
        var instance = FcmPush()
    }
    init {
        gson = Gson()
        okhttpClient = OkHttpClient()
    }
    fun sendMessage (destinationUid : String, title : String, message : String){
        FirebaseFirestore.getInstance().collection("pushtokens").document().get().addOnCompleteListener {
            task ->
            if(task.isSuccessful){
                var token = task?.result?.get("pushTokens").toString()

                var pushDTO = PushDTO()
                pushDTO.to = token
                pushDTO.notification.title = title
                pushDTO.notification.body = message

                var body = RequestBody.create(JSON, gson?.toJson(pushDTO))
                var request = Request.Builder()
                        .addHeader("Content-Type", "application/json")
                        .addHeader("Authorization", "key="+serverKey)
                        .url(url)
                        .post(body)
                        .build()

                okhttpClient?.newCall(request)?.enqueue(object : Callback{
                    override fun onFailure(call: Call?, e: IOException?) {
                        //요청이 실패했을때

                    }

                    override fun onResponse(call: Call?, response: Response?) {
                        //요청이 성공했을때
                        println(request.body()?.toString())
                    }

                })

            }
        }
    }
}