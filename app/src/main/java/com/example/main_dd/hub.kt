package com.example.main_dd

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.main_dd.ui.theme.Main_ddTheme


// ==========================================
// HUB
// ==========================================

class Hub : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            Main_ddTheme {
                HubScreen()
            }
        }
    }
}


// ==========================================
// ОСНОВНОЙ ЭКРАН
// ==========================================

@Composable
fun HubScreen() {

    // ======================================
    // CONTEXT ANDROID
    // ======================================

    val context = androidx.compose.ui.platform.LocalContext.current


    // ======================================
    // ПОЛУЧАЕМ РЕАЛЬНЫЕ ЧАТЫ С СЕРВЕРА
    // ======================================

    val chats = ServerConnection.chats


    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        // ==================================
        // ФОНОВАЯ КАРТИНКА
        // ==================================

        Image(
            painter = painterResource(
                id = R.drawable.greenfon
            ),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )


        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = 12.dp,
                    end = 12.dp,
                    top = 35.dp,
                    bottom = 55.dp
                )
        ) {

            // ==================================
            // ЗАГОЛОВОК
            // ==================================

            Text(
                text = "диалог > ~ <",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,

                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        bottom = 25.dp
                    )
            )


            // ==================================
            // СПИСОК РЕАЛЬНЫХ ЧАТОВ
            // ==================================

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),

                verticalArrangement = Arrangement.spacedBy(
                    10.dp
                )
            ) {

                items(
                    items = chats
                ) { chat ->

                    ChatItem(
                        chat = chat,

                        onClick = {

                            // ==================================
                            // ОТКРЫВАЕМ ЭТОТ ЧАТ
                            // ==================================

                            openChat(
                                context = context,
                                chat = chat
                            )
                        }
                    )
                }
            }


            Spacer(
                modifier = Modifier.height(12.dp)
            )


            // ==================================
            // НОВЫЙ КОНТАКТ
            // ==================================

            Button(
                onClick = {

                    // ПОЗЖЕ

                },

                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),

                shape = RoundedCornerShape(25.dp),

                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xE6191919)
                )
            ) {

                Text(
                    text = "новый контакт +",
                    color = Color.White,
                    fontSize = 17.sp
                )
            }
        }
    }
}


// ==========================================
// ОТКРЫТЬ КОНКРЕТНЫЙ ЧАТ
// ==========================================

fun openChat(
    context: android.content.Context,
    chat: ServerChat
) {

    val intent = Intent(
        context,
        Msg::class.java
    )


    // ======================================
    // ПЕРЕДАЁМ ДАННЫЕ В MSG
    // ======================================

    intent.putExtra(
        "chat_id",
        chat.chatId
    )

    intent.putExtra(
        "unic_id",
        chat.unicId
    )

    intent.putExtra(
        "nickname",
        chat.nickname
    )

    intent.putExtra(
        "ava",
        chat.ava
    )


    // ======================================
    // ОТКРЫВАЕМ MSG
    // ======================================

    context.startActivity(
        intent
    )
}


// ==========================================
// ОДИН РЕАЛЬНЫЙ ЧАТ
// ==========================================

@Composable
fun ChatItem(
    chat: ServerChat,
    onClick: () -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(78.dp)
            .clip(
                RoundedCornerShape(18.dp)
            )
            .background(
                Color(0xD9141414)
            )
            .clickable {
                onClick()
            }
            .padding(
                horizontal = 12.dp,
                vertical = 10.dp
            ),

        verticalAlignment = Alignment.CenterVertically
    ) {

        // ==================================
        // АВАТАР
        // ==================================

        Box(
            modifier = Modifier
                .size(55.dp)
                .clip(CircleShape)
                .background(
                    Color(0xFF464646)
                ),

            contentAlignment = Alignment.Center
        ) {

            Image(
                painter = painterResource(
                    id = getAvatarResource(
                        chat.ava
                    )
                ),

                contentDescription = null,

                modifier = Modifier
                    .fillMaxSize(),

                contentScale = ContentScale.Crop
            )
        }


        // ==================================
        // ИНФОРМАЦИЯ О ПОЛЬЗОВАТЕЛЕ
        // ==================================

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(
                    start = 12.dp
                ),

            verticalArrangement = Arrangement.Center
        ) {

            Text(
                text = chat.nickname,
                color = Color.White,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}


// ==========================================
// ПОЛУЧЕНИЕ РЕСУРСА АВАТАРА
// ==========================================

fun getAvatarResource(
    avatar: Int
): Int {

    return when (avatar) {

        0 -> R.drawable.`ava0`
        1 -> R.drawable.`ava1`
        2 -> R.drawable.`ava2`
        3 -> R.drawable.`ava3`
        4 -> R.drawable.`ava4`
        5 -> R.drawable.`ava5`
        6 -> R.drawable.`ava6`
        7 -> R.drawable.`ava7`
        8 -> R.drawable.`ava8`
        9 -> R.drawable.`ava9`
        10 -> R.drawable.`ava10`
        11 -> R.drawable.`ava11`
        12 -> R.drawable.`ava12`

        else -> R.drawable.`ava0`
    }
}


// ==========================================
// PREVIEW
// ==========================================

@Preview(
    showBackground = true,
    showSystemUi = true
)
@Composable
fun HubPreview() {

    Main_ddTheme {
        HubScreen()
    }
}