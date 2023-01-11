package com.example.mychatbottutorial.data.model

enum class MsgType {
    Me, BOT
}

//메세지 데이터 클래스
data class Msg(val content : String, val type: MsgType)