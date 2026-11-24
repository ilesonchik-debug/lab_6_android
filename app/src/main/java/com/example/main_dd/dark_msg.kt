package com.example.main_dd

import okio.ByteString
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer


// ==========================================
// ЛОГИКА СООБЩЕНИЙ
// ==========================================

object DarkMsg {

    // ======================================
    // ПОЛУЧИТЬ СООБЩЕНИЯ ЭТОГО ЧАТА
    // ======================================

    fun getMessages(
        chatId: String
    ): List<ServerMessage> {

        return ServerConnection.messages.filter {

            it.chatId == chatId
        }
    }


    // ======================================
    // КОЛИЧЕСТВО СООБЩЕНИЙ
    // ======================================

    fun getMessageCount(
        chatId: String
    ): Int {

        return ServerConnection.messages.count {

            it.chatId == chatId
        }
    }


    // ======================================
    // ПРОВЕРКА СООБЩЕНИЯ
    // ПРИНАДЛЕЖИТ ЛИ ОНО ЭТОМУ ЧАТУ
    // ======================================

    fun isMessageFromChat(
        message: ServerMessage,
        chatId: String
    ): Boolean {

        return message.chatId == chatId
    }


    // ======================================
    // МОЁ СООБЩЕНИЕ
    // ======================================

    fun isMyMessage(
        message: ServerMessage,
        currentUnicId: String
    ): Boolean {

        return message.kto == currentUnicId
    }


    // ======================================
    // ВХОДЯЩЕЕ СООБЩЕНИЕ
    // ======================================

    fun isIncomingMessage(
        message: ServerMessage,
        currentUnicId: String
    ): Boolean {

        return message.kto != currentUnicId
    }


    // ======================================
    // МОИ СООБЩЕНИЯ В ЧАТЕ
    // ======================================

    fun getMyMessages(
        chatId: String,
        currentUnicId: String
    ): List<ServerMessage> {

        return ServerConnection.messages.filter {

            it.chatId == chatId &&
                    it.kto == currentUnicId
        }
    }


    // ======================================
    // ВХОДЯЩИЕ СООБЩЕНИЯ В ЧАТЕ
    // ======================================

    fun getIncomingMessages(
        chatId: String,
        currentUnicId: String
    ): List<ServerMessage> {

        return ServerConnection.messages.filter {

            it.chatId == chatId &&
                    it.kto != currentUnicId
        }
    }


    // ======================================
    // ПОСЛЕДНЕЕ СООБЩЕНИЕ
    // ======================================

    fun getLastMessage(
        chatId: String
    ): ServerMessage? {

        return ServerConnection.messages.lastOrNull {

            it.chatId == chatId
        }
    }


    // ======================================
    // WEBSOCKET
    // ======================================

    fun getConnection(): okhttp3.WebSocket? {

        return ServerConnection.getConnection()
    }


    fun isConnected(): Boolean {

        return ServerConnection.isConnected()
    }


    // ======================================
    // ОТПРАВИТЬ СООБЩЕНИЕ
    //
    // ПАКЕТ:
    //
    // a
    // + длина unic отправителя 1 байт
    // + unic отправителя
    // + длина unic получателя 1 байт
    // + unic получателя
    // + длина chat_id 1 байт
    // + chat_id
    // + длина текста 2 байта
    // + текст UTF-8
    // + type 1 байт
    //
    // type = 0
    // ======================================

    fun sendMessage(
        chatId: String,
        recipientUnic: String,
        text: String
    ): Boolean {

        // ==================================
        // ПОЛУЧАЕМ НАШ UNIC
        // ==================================

        val senderUnic =
            ServerConnection.myUnic


        // ==================================
        // ПРОВЕРЯЕМ ЧТО МЫ АВТОРИЗОВАНЫ
        // ==================================

        if (senderUnic.isNullOrEmpty()) {

            println(
                "ОШИБКА ОТПРАВКИ: наш unic отсутствует"
            )

            return false
        }


        // ==================================
        // ПРОВЕРЯЕМ ПОЛУЧАТЕЛЯ
        // ==================================

        if (recipientUnic.isEmpty()) {

            println(
                "ОШИБКА ОТПРАВКИ: unic получателя пустой"
            )

            return false
        }


        // ==================================
        // ПРОВЕРЯЕМ CHAT ID
        // ==================================

        if (chatId.isEmpty()) {

            println(
                "ОШИБКА ОТПРАВКИ: chat_id пустой"
            )

            return false
        }


        // ==================================
        // УБИРАЕМ ЛИШНИЕ ПРОБЕЛЫ
        // ==================================

        val cleanText =
            text.trim()


        // ==================================
        // ПУСТОЕ СООБЩЕНИЕ НЕ ОТПРАВЛЯЕМ
        // ==================================

        if (cleanText.isEmpty()) {

            println(
                "Сообщение пустое — отправка отменена"
            )

            return false
        }


        // ==================================
        // UTF-8
        // ==================================

        val senderBytes =
            senderUnic.toByteArray(Charsets.UTF_8)

        val recipientBytes =
            recipientUnic.toByteArray(Charsets.UTF_8)

        val chatIdBytes =
            chatId.toByteArray(Charsets.UTF_8)

        val textBytes =
            cleanText.toByteArray(Charsets.UTF_8)


        // ==================================
        // ПРОВЕРКА UNIC
        //
        // ДЛИНА ХРАНИТСЯ В 1 БАЙТЕ
        // ==================================

        if (senderBytes.size > 255) {

            println(
                "ОШИБКА: наш unic слишком длинный"
            )

            return false
        }


        if (recipientBytes.size > 255) {

            println(
                "ОШИБКА: unic получателя слишком длинный"
            )

            return false
        }


        // ==================================
        // ПРОВЕРКА CHAT ID
        //
        // ДЛИНА ХРАНИТСЯ В 1 БАЙТЕ
        // ==================================

        if (chatIdBytes.size > 255) {

            println(
                "ОШИБКА: chat_id слишком длинный"
            )

            return false
        }


        // ==================================
        // МАКСИМУМ 3000 СИМВОЛОВ
        // ==================================

        if (cleanText.length > 3000) {

            println(
                "ОШИБКА: сообщение длиннее 3000 символов"
            )

            return false
        }


        // ==================================
        // ПРОВЕРЯЕМ РАЗМЕР UTF-8
        //
        // ДЛИНА В ПАКЕТЕ = 2 БАЙТА
        // ==================================

        if (textBytes.size > 65535) {

            println(
                "ОШИБКА: сообщение слишком большое"
            )

            return false
        }


        // ==================================
        // СОЗДАЁМ ПАКЕТ
        // ==================================

        val output =
            ByteArrayOutputStream()


        // ==================================
        // ПЕРВЫЙ БАЙТ
        // 'a'
        // ==================================

        output.write(
            'a'.code
        )


        // ==================================
        // UNIC ОТПРАВИТЕЛЯ
        // ==================================

        output.write(
            senderBytes.size
        )

        output.write(
            senderBytes
        )


        // ==================================
        // UNIC ПОЛУЧАТЕЛЯ
        // ==================================

        output.write(
            recipientBytes.size
        )

        output.write(
            recipientBytes
        )


        // ==================================
        // CHAT ID
        // ==================================

        output.write(
            chatIdBytes.size
        )

        output.write(
            chatIdBytes
        )


        // ==================================
        // ДЛИНА ТЕКСТА
        //
        // 2 БАЙТА
        // ==================================

        val textLengthBuffer =
            ByteBuffer.allocate(2)

        textLengthBuffer.putShort(
            textBytes.size.toShort()
        )

        output.write(
            textLengthBuffer.array()
        )


        // ==================================
        // ТЕКСТ
        // ==================================

        output.write(
            textBytes
        )


        // ==================================
        // TYPE
        //
        // 0 = обычное сообщение
        // ==================================

        output.write(
            0
        )


        // ==================================
        // ПОЛУЧАЕМ ГОТОВЫЙ ПАКЕТ
        // ==================================

        val packet =
            output.toByteArray()


        // ==================================
        // ПОЛУЧАЕМ WEBSOCKET
        // ==================================

        val webSocket =
            ServerConnection.getConnection()


        if (webSocket == null) {

            println(
                "ОШИБКА ОТПРАВКИ: WebSocket отсутствует"
            )

            return false
        }


        // ==================================
        // ОТПРАВЛЯЕМ BINARY
        // ==================================

        val result =
            webSocket.send(
                ByteString.of(*packet)
            )


        // ==================================
        // ОТЛАДКА
        // ==================================

        println(
            "================================="
        )

        println(
            "ОТПРАВКА СООБЩЕНИЯ"
        )

        println(
            "Тип: a"
        )

        println(
            "Отправитель: $senderUnic"
        )

        println(
            "Получатель: $recipientUnic"
        )

        println(
            "Chat ID: $chatId"
        )

        println(
            "Текст: $cleanText"
        )

        println(
            "Тип сообщения: 0"
        )

        println(
            "Размер UTF-8 текста: ${textBytes.size}"
        )

        println(
            "Размер пакета: ${packet.size}"
        )

        println(
            "WebSocket send result: $result"
        )

        println(
            "================================="
        )


        return result
    }
}


