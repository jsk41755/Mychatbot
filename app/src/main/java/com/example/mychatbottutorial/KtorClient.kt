package com.example.mychatbottutorial

import android.util.Log
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import kotlin.math.log

//메모리를 한 곳으로만 사용할 수 있게 도와주는 싱글톤 클래스
object KtorClient {

    const val TAG = "API체크"

    const val BASE_URL = "https://wsapi.simsimi.com/190410/talk"
    //api키는 외부에서 다루지 않기 때문에 private로 설정
    private const val API_KEY = "18VDASGFX2HZJhjsID93ll0YZd4qHkGUqSDX-VWC"


    val httpClient = HttpClient(CIO){

        install(ContentNegotiation){
            json(Json{
                encodeDefaults = true
                ignoreUnknownKeys = true
            })
        }

        //API 로그 설정
        install(Logging){
            logger = object : Logger{
                override fun log(message: String) {
                    Log.d(TAG, "api log: $message")
                }
            }
            level = LogLevel.ALL
        }

        //타임아웃 설정
        install(HttpTimeout){
            requestTimeoutMillis = 15_000
            connectTimeoutMillis = 15_000
            socketTimeoutMillis = 15_000
        }

        //-H "Content-Type: application/json" \
        //     -H "x-api-key: PASTE_YOUR_PROJECT_KEY_HERE" \
        // 기본 request 설정
        defaultRequest {
            headers.append("Content-Type", "application/json")
            headers.append("x-api-key", API_KEY)
        }
    }
}