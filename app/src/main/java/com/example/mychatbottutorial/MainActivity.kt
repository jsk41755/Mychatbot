package com.example.mychatbottutorial

import android.os.Bundle
import android.os.Message
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.modifier.ModifierLocalReadScope
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mychatbottutorial.MainActivity.Companion.TAG
import com.example.mychatbottutorial.data.model.Msg
import com.example.mychatbottutorial.data.model.MsgType
import com.example.mychatbottutorial.ui.theme.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    companion object{
        const val TAG = "챗봇"
    }

    private val chatBotViewModel: ChatBotViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        setContent {
            MyChatbotTutorialTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    ChatBotView(chatBotViewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ChatBotView(chatBotViewModel: ChatBotViewModel){

    //뷰 모델에 있는 플로우형태를 콜렉트를 통해서 데이터를 받는데, 연결을 짓는 역할, 리컴포즈
    val userInput : State<String> = chatBotViewModel.userInputFlow.collectAsState()

    var isSendBtnEnable : State<Boolean> = chatBotViewModel.isSendBtnEnableFlow.collectAsState()

    //사용자 입력 상태
    /*var userInput by remember {
        mutableStateOf("사용자 입력")
    }*/

    //메세지들
    val messages : State<List<Msg>> = chatBotViewModel.messagesFlow.collectAsState()

    //로딩중
    val isLoading : State<Boolean> = chatBotViewModel.isLoading.collectAsState()

    val numbersRange = (1..100)

    val dummyMessages = numbersRange.mapIndexed{ index, number ->

        val type = if (index % 2 == 0) MsgType.BOT else MsgType.Me

        Msg("$number 번째 입니다!", type)
    }

    val coroutineScope = rememberCoroutineScope() //스크롤 관련 코루틴 스코프
    val scrollState = rememberLazyListState() // 스크롤 맨 아래로

    val focusManager = LocalFocusManager.current //현재 애를 가져옴.

    LaunchedEffect(key1 = Unit){
        chatBotViewModel.msgReceivedEvent.collectLatest {
            scrollState.animateScrollToItem(messages.value.size)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        LazyColumn(
            state = scrollState,
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp,
                alignment = Alignment.Top),
            contentPadding = PaddingValues(10.dp)
        ){
            items(messages.value){ aMessage : Msg ->
                MsgRowItem(aMessage)
            }

        }

        Divider(
            startIndent = 0.dp,
            thickness = 1.dp,
            color = Color.LightGray
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max)
                .background(Color.White),
            verticalAlignment = Alignment.Top
        ) {
            BasicTextField(
                modifier = Modifier
                    .background(Color.White)
                    .weight(1f)
                    .padding(12.dp),
                value = userInput.value,
                onValueChange = { justTypedUserInput ->
                chatBotViewModel.userInputFlow.value = justTypedUserInput
            })
            Card(
                backgroundColor = if (isSendBtnEnable.value)sendBtnBGColor else Color.LightGray,
                onClick = {
                    Log.d(TAG, "전송하기 버튼 클릭!")
                    if(!isSendBtnEnable.value){
                        return@Card
                    }
                    chatBotViewModel.sendMessage()
                    /*
                    if(userInput.value.isEmpty()){
                        return@Card
                    }
                    messages.value += Msg(userInput.value, MsgType.Me)
                    userInput = ""*/
                    focusManager.clearFocus()
                    coroutineScope.launch {
                        scrollState.animateScrollToItem(messages.value.size)
                    }
                }) {
                    if(isLoading.value){
                        CircularProgressIndicator(
                            color = BotBubbleColor,
                            modifier = Modifier
                                .padding(5.dp)
                                .scale(0.6f)
                                .requiredHeight(IntrinsicSize.Max)
                        )
                    } else{
                        Image(
                            painter = painterResource(id = R.drawable.ic_send_message),
                            contentDescription = "보내기 버튼",
                            modifier = Modifier
                                .padding(10.dp)
                                .width(30.dp))
                    }

            }
        }
    }
}

//분기처리만
@Composable
fun MsgRowItem(data : Msg){

    val itemArrangement : (MsgType) -> Arrangement.Horizontal = {
        if(data.type == MsgType.Me) Arrangement.End else Arrangement.Start
    }

    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = itemArrangement(data.type)
    ) {
        when(data.type){
            MsgType.Me -> MeMsgRowItem(message = data.content)
            MsgType.BOT -> BotMsgRowItem(message = data.content)
        }
    }
}

@Composable
fun MeMsgRowItem(message: String){
    
    Row(
        modifier = Modifier
            .height(IntrinsicSize.Max)
            .fillMaxWidth(0.7f)
            .padding(0.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.End
    ) {

        Box(
            modifier = Modifier
                .background(
                    color = MeBubbleColor,
                    shape = RoundedCornerShape(12.dp, 12.dp, 0.dp, 12.dp)
                )
                .width(IntrinsicSize.Max)
        ){
            Text(text = message, modifier = Modifier.padding(16.dp))
        }

        Box(
            modifier = Modifier
                .background(
                    color = MeBubbleColor,
                    shape = TriangleShapeForRight(30)
                )
                .width(30.dp)
                .fillMaxHeight()
        ){        }
    }
    

}

@Composable
fun BotMsgRowItem(message: String){
    Row(
        modifier = Modifier
            .height(IntrinsicSize.Max)
            .fillMaxWidth(0.7f),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.Start
    ) {

        Image(
            painter = painterResource(id = R.drawable.robot),
            contentDescription = "bot",
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .border(BorderStroke(2.dp, Color.LightGray), shape = CircleShape)
        )
        
        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .background(
                    color = BotBubbleColor,
                    shape = TriangleShapeForLeft(30)
                )
                .width(10.dp)
                .fillMaxHeight()
        ){        }

        Box(
            modifier = Modifier
                .background(
                    color = BotBubbleColor,
                    shape = RoundedCornerShape(12.dp, 12.dp, 12.dp, 0.dp)
                )
                .width(IntrinsicSize.Max)
        ){
            Text(text = message, modifier = Modifier.padding(16.dp), color = Color.White)
        }

    }
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MyChatbotTutorialTheme {
        Greeting("Android")
    }
}