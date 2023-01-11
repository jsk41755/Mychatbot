package com.example.mychatbottutorial

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mychatbottutorial.data.model.Msg
import com.example.mychatbottutorial.data.model.MsgType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChatBotViewModel : ViewModel() {

    //사용자 입력
    val userInputFlow = MutableStateFlow("")

    //상태를 알고 있는 플로우(보내기 버튼 비활성화 여부)
    val isSendBtnEnableFlow = MutableStateFlow(false)

    //메세지들
    val messagesFlow = MutableStateFlow<List<Msg>>(emptyList())

    //로딩중
    val isLoading = MutableStateFlow(false)

    //챗봇 메세지 받음 - 단발성 이벤트
    val msgReceivedEvent = MutableSharedFlow<Unit>()

    companion object{
        const val TAG = "ChatBotViewModel"
    }

    init {
        viewModelScope.launch {
            userInputFlow.collectLatest {
                Log.d(TAG, "입력된 값 : $it")
                isSendBtnEnableFlow.emit(it.isNotEmpty())
            }
        }
    }

    //API를 자동으로 하게 됨.
    fun sendMessage(){
        viewModelScope.launch {
            callSendMessage()
        }
    }

    //suspend fun하는 이유는 코루틴으로 사용하기 때문
    private suspend fun callSendMessage(){
        Log.d(TAG, "ChatBotViewModel- callSendMessage() called")

        if (userInputFlow.value.isEmpty()){
            return
        }

        if (isLoading.value){
            return
        }

        //코루틴에서 런칭되는 것을 에러를 잡는 것
        withContext(Dispatchers.IO){
            kotlin.runCatching{
                //내가 메세지를 보냈을 때
                messagesFlow.value += Msg(userInputFlow.value, MsgType.Me)
                isLoading.emit(true)
                MessageRepository.sendMessage(userInputFlow.value)//사용자 입력을 가짐.
            }.onSuccess {
                messagesFlow.value += it
                userInputFlow.value = ""
                isLoading.emit(false)
                msgReceivedEvent.emit(Unit)
                Log.d(TAG, "ChatBotViewModel- onSuccess() called")
            }.onFailure {
                Log.d(TAG, "ChatBotViewModel- onFailure() error: ${it.localizedMessage}")
                isLoading.value = false
            }
        }

        MessageRepository.sendMessage(userInputFlow.value)
    }

}