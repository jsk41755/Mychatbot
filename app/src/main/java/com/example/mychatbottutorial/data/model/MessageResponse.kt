package com.example.mychatbottutorial.data.model

import kotlinx.serialization.Serializable

@kotlinx.serialization.Serializable
data class MessageResponse (
    val status: Long,
    val statusMessage: String,
    val request: Request,
    val atext: String,
    val lang: String
)

//Serializable는 annotation형태로써 Json형태로 만들어줌
@Serializable
data class Request (
    val utext: String,
    val lang: String
)
//확장함수
fun MessageResponse.toMsg() : Msg {
    return Msg(this.atext, type = MsgType.BOT)
}
