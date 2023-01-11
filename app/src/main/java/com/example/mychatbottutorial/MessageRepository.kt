package com.example.mychatbottutorial

import com.example.mychatbottutorial.data.model.MessageResponse
import com.example.mychatbottutorial.data.model.Msg
import com.example.mychatbottutorial.data.model.Request
import com.example.mychatbottutorial.data.model.toMsg
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

//서버 API를 요청하는 것
object MessageRepository {

    //api 요청을 하는 함수
    //메세지를 받고 1.
   suspend fun sendMessage(userInput : String) : Msg{

        //변수로 받아뒀던 것을 return으로 바로 감.
        val receivedMsg =

            //메세지를 반환 2.
        return KtorClient.httpClient.post(KtorClient.BASE_URL){
            contentType(ContentType.Application.Json)
            setBody(Request(utext = userInput, lang = "ko"))
        }.body<MessageResponse>().toMsg()
    }
}