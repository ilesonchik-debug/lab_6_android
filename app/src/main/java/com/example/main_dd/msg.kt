package com.example.main_dd

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.main_dd.ui.theme.Main_ddTheme


// ==========================================
// ЭКРАН СООБЩЕНИЙ
// ==========================================

class Msg : ComponentActivity() {

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(
            savedInstanceState
        )


        enableEdgeToEdge()


        // ==================================
        // ПОЛУЧАЕМ ДАННЫЕ ВЫБРАННОГО ЧАТА
        // ==================================

        val chatId =
            intent.getStringExtra("chat_id")
                ?: ""

        val unicId =
            intent.getStringExtra("unic_id")
                ?: ""

        val nickname =
            intent.getStringExtra("nickname")
                ?: ""

        val ava =
            intent.getIntExtra(
                "ava",
                0
            )


        // ==================================
        // ОТКРЫВАЕМ ЭКРАН
        // ==================================

        setContent {

            Main_ddTheme {

                MsgScreen(
                    chatId = chatId,
                    unicId = unicId,
                    userName = nickname,
                    ava = ava
                )
            }
        }
    }
}


// ==========================================
// ЭКРАН ЧАТА
// ==========================================

@Composable
fun MsgScreen(
    chatId: String,
    unicId: String,
    userName: String,
    ava: Int
) {

    // ======================================
    // CONTEXT
    // ======================================

    val context = LocalContext.current


    // ======================================
    // ТЕКСТ ПОЛЯ ВВОДА
    // ======================================

    var messageText by remember {

        mutableStateOf("")
    }


    // ======================================
    // ПОЛУЧАЕМ СООБЩЕНИЯ ИМЕННО ЭТОГО ЧАТА
    // ======================================

    val messages =
        ServerConnection.messages.filter {

            it.chatId == chatId
        }


    // ======================================
    // СОСТОЯНИЕ СПИСКА
    // ======================================

    val listState =
        rememberLazyListState()


    // ======================================
    // АВТОМАТИЧЕСКАЯ ПРОКРУТКА ВНИЗ
    //
    // ПРИ ОТКРЫТИИ ЧАТА
    // И ПРИ ПОЯВЛЕНИИ НОВОГО СООБЩЕНИЯ
    // ======================================

    LaunchedEffect(
        chatId,
        messages.size
    ) {

        if (messages.isNotEmpty()) {

            listState.animateScrollToItem(
                index = messages.lastIndex
            )
        }
    }


    // ======================================
    // ОСНОВНОЙ ЭКРАН
    // ======================================

    Box(
        modifier = Modifier.fillMaxSize()
    ) {


        // ==================================
        // ФОН
        // ==================================

        Image(
            painter = painterResource(
                id = R.drawable.greenfon
            ),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )


        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
        ) {

            // ==================================
            // НИЖНЕЕ ПРОСТРАНСТВО
            // ==================================

            val bottomSpace =
                maxHeight * 0.10f


            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        start = 12.dp,
                        end = 12.dp,
                        top = 35.dp
                    )
            ) {


                // ==================================
                // ВЕРХНЯЯ СТРОКА
                //
                // < + АВАТАР + NICKNAME
                // ==================================

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp),

                    verticalAlignment =
                        Alignment.CenterVertically
                ) {


                    // ==================================
                    // КНОПКА НАЗАД
                    // ==================================

                    Button(
                        onClick = {

                            (context as? Activity)?.finish()

                        },

                        modifier =
                            Modifier.size(48.dp),

                        shape =
                            RoundedCornerShape(14.dp),

                        contentPadding =
                            PaddingValues(0.dp),

                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor =
                                    Color(0xFF641B1B)
                            )
                    ) {

                        Text(
                            text = "<",
                            color = Color.White,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }


                    Spacer(
                        modifier =
                            Modifier.size(10.dp)
                    )


                    // ==================================
                    // АВАТАР
                    // ==================================

                    Image(
                        painter = painterResource(
                            id = getAvatarResource(ava)
                        ),

                        contentDescription = null,

                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape),

                        contentScale =
                            ContentScale.Crop
                    )


                    Spacer(
                        modifier =
                            Modifier.size(10.dp)
                    )


                    // ==================================
                    // NICKNAME
                    // ==================================

                    Text(
                        text = userName,
                        color = Color.White,
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.weight(1f)
                    )
                }


                Spacer(
                    modifier =
                        Modifier.height(15.dp)
                )


                // ==================================
                // СПИСОК СООБЩЕНИЙ
                //
                // СТАРЫЕ СВЕРХУ
                // НОВЫЕ СНИЗУ
                // ==================================

                LazyColumn(
                    state = listState,

                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),

                    verticalArrangement =
                        Arrangement.spacedBy(8.dp)
                ) {

                    items(
                        items = messages
                    ) { message ->

                        MessageItem(
                            message = message,

                            myUnicId =
                                ServerConnection.myUnic
                                    ?: ""
                        )
                    }
                }


                Spacer(
                    modifier =
                        Modifier.height(10.dp)
                )


                // ==================================
                // ПОЛЕ ВВОДА + КНОПКА
                // ==================================

                Row(
                    modifier =
                        Modifier.fillMaxWidth(),

                    verticalAlignment =
                        Alignment.CenterVertically
                ) {


                    // ==================================
                    // ПОЛЕ СООБЩЕНИЯ
                    // ==================================

                    OutlinedTextField(
                        value = messageText,

                        onValueChange = {

                            messageText = it
                        },

                        modifier = Modifier
                            .weight(1f)
                            .height(58.dp),

                        placeholder = {

                            Text(
                                text = "сообщение",
                                color = Color.LightGray
                            )
                        },

                        singleLine = true,

                        keyboardOptions =
                            KeyboardOptions(
                                keyboardType =
                                    KeyboardType.Text
                            ),

                        shape =
                            RoundedCornerShape(18.dp),

                        colors =
                            androidx.compose.material3
                                .OutlinedTextFieldDefaults
                                .colors(

                                    focusedTextColor =
                                        Color.White,

                                    unfocusedTextColor =
                                        Color.White,

                                    focusedContainerColor =
                                        Color(0xE6191919),

                                    unfocusedContainerColor =
                                        Color(0xE6191919),

                                    focusedBorderColor =
                                        Color.White,

                                    unfocusedBorderColor =
                                        Color.LightGray,

                                    cursorColor =
                                        Color.White
                                )
                    )


                    Spacer(
                        modifier =
                            Modifier.size(8.dp)
                    )


                    // ==================================
                    // КНОПКА ОТПРАВКИ
                    // ==================================

                    Button(
                        onClick = {

                            val sent =
                                DarkMsg.sendMessage(
                                    chatId = chatId,
                                    recipientUnic = unicId,
                                    text = messageText
                                )


                            // ==============================
                            // ЕСЛИ СООБЩЕНИЕ УСПЕШНО
                            // ОТПРАВЛЕНО
                            // ==============================

                            if (sent) {

                                messageText = ""
                            }

                        },

                        modifier =
                            Modifier.size(58.dp),

                        shape =
                            RoundedCornerShape(16.dp),

                        contentPadding =
                            PaddingValues(0.dp),

                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor =
                                    Color(0xFF159A82),

                                contentColor =
                                    Color.White
                            )
                    ) {

                        Text(
                            text = ">",
                            color = Color.White,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }


                // ==================================
                // ПУСТОЕ ПРОСТРАНСТВО СНИЗУ
                // 10%
                // ==================================

                Spacer(
                    modifier =
                        Modifier.height(
                            bottomSpace
                        )
                )
            }
        }
    }
}


// ==========================================
// ОДНО СООБЩЕНИЕ
// ==========================================
// ==========================================
// ОДНО СООБЩЕНИЕ
// ==========================================

@Composable
fun MessageItem(
    message: ServerMessage,
    myUnicId: String
) {

    // ======================================
    // ОПРЕДЕЛЯЕМ ЧЬЁ СООБЩЕНИЕ
    // ======================================

    val isMine =
        myUnicId.isNotEmpty() &&
                message.kto == myUnicId


    Row(
        modifier =
            Modifier.fillMaxWidth(),

        horizontalArrangement =
            if (isMine) {

                Arrangement.End

            } else {

                Arrangement.Start
            }
    ) {

        // ==================================
        // ПУЗЫРЬ СООБЩЕНИЯ
        // ==================================

        Column(
            modifier = Modifier
                .clip(
                    RoundedCornerShape(18.dp)
                )
                .background(

                    if (isMine) {

                        Color(0xFF075E54)

                    } else {

                        Color(0xD9464646)
                    }
                )
                .padding(
                    start = 14.dp,
                    top = 9.dp,
                    end = 9.dp,
                    bottom = 6.dp
                )
        ) {

            // ==================================
            // ТЕКСТ СООБЩЕНИЯ
            // ==================================

            Text(
                text = message.text,
                color = Color.White,
                fontSize = 16.sp
            )


            // ==================================
            // ДАТА + ВРЕМЯ
            // БЕЗ СЕКУНД
            // ==================================

            Spacer(
                modifier =
                    Modifier.height(3.dp)
            )


            Row(
                modifier =
                    Modifier.align(
                        Alignment.End
                    ),

                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Text(
                    text =
                        formatMessageTime(
                            message.time
                        ),

                    color =
                        Color.White.copy(
                            alpha = 0.55f
                        ),

                    fontSize = 10.sp
                )
            }
        }
    }
}


// ==========================================
// ФОРМАТИРОВАНИЕ ВРЕМЕНИ
//
// 17:42:35
// ↓
// 17:42
// ==========================================

fun formatMessageTime(
    time: String
): String {

    val parts =
        time.split(":")

    return if (parts.size >= 2) {

        "${parts[0]}:${parts[1]}"

    } else {

        time
    }
}


// ==========================================
// ПРЕДПРОСМОТР
// ==========================================

@Preview(
    showBackground = true,
    showSystemUi = true
)
@Composable
fun MsgPreview() {

    Main_ddTheme {

        MsgScreen(
            chatId = "preview_chat",
            unicId = "preview_unic",
            userName = "empty",
            ava = 0
        )
    }
}