package com.example.main_dd

import androidx.compose.runtime.mutableStateListOf
import okhttp3.WebSocket
import java.nio.ByteBuffer


// ==========================================
// ЧАТ, ПОЛУЧЕННЫЙ С СЕРВЕРА
// ==========================================

data class ServerChat(
    val chatId: String,
    val unicId: String,
    val nickname: String,
    val ava: Int
)


// ==========================================
// СООБЩЕНИЕ, ПОЛУЧЕННОЕ С СЕРВЕРА
// ==========================================

data class ServerMessage(
    val idMsg: String,
    val data: String,
    val time: String,
    val kto: String,
    val komu: String,
    val type: String,
    val text: String,
    val chatId: String
)


// ==========================================
// СОЕДИНЕНИЕ С СЕРВЕРОМ
// ==========================================

object ServerConnection {

    // ======================================
    // WEBSOCKET СОЕДИНЕНИЕ
    // ======================================

    private var webSocket: WebSocket? = null


    // ======================================
    // ДАННЫЕ ТЕКУЩЕГО АВТОРИЗОВАННОГО
    // ПОЛЬЗОВАТЕЛЯ
    // ======================================

    var myUnic: String? = null

    var myAva: String? = null

    var myNik: String? = null


    // ======================================
    // ПОЛУЧЕННЫЕ ЧАТЫ
    //
    // ЭТОТ СПИСОК НАБЛЮДАЕТ COMPOSE
    // ======================================

    val chats = mutableStateListOf<ServerChat>()


    // ======================================
    // ПОЛУЧЕННЫЕ СООБЩЕНИЯ
    //
    // ЭТОТ СПИСОК ТОЖЕ НАБЛЮДАЕТ COMPOSE
    // ======================================

    val messages = mutableStateListOf<ServerMessage>()


    // ======================================
    // СОХРАНИТЬ СОЕДИНЕНИЕ
    // ======================================

    fun setConnection(
        connection: WebSocket
    ) {

        webSocket = connection
    }


    // ======================================
    // ПОЛУЧИТЬ СОЕДИНЕНИЕ
    // ======================================

    fun getConnection(): WebSocket? {

        return webSocket
    }


    // ======================================
    // ПРОВЕРКА ПОДКЛЮЧЕНИЯ
    // ======================================

    fun isConnected(): Boolean {

        return webSocket != null
    }


    // ======================================
    // ОЧИСТИТЬ ЧАТЫ
    // ======================================

    fun clearChats() {

        chats.clear()
    }


    // ======================================
    // ОЧИСТИТЬ СООБЩЕНИЯ
    // ======================================

    fun clearMessages() {

        messages.clear()
    }


    // ======================================
    // ОЧИСТИТЬ ВСЕ ДАННЫЕ
    // ======================================

    fun clearAll() {

        chats.clear()

        messages.clear()

        myUnic = null

        myAva = null

        myNik = null
    }


    // ======================================
    // ПРИНЯТЬ ДАННЫЕ ТЕКУЩЕГО ПОЛЬЗОВАТЕЛЯ
    //
    // ПАКЕТ:
    //
    // AUTH_USER
    // + длина unic 2 байта
    // + unic
    // + длина ava 2 байта
    // + ava
    // + длина nik 2 байта
    // + nik
    // ======================================

    fun receiveMyUser(
        data: ByteArray
    ) {

        try {

            println(
                "================================="
            )

            println(
                "Получен пакет данных пользователя"
            )

            println(
                "Размер: ${data.size} байт"
            )


            // ==================================
            // ПРОВЕРЯЕМ ЧТО ПАКЕТ НЕ ПУСТОЙ
            // ==================================

            if (data.isEmpty()) {

                println(
                    "Ошибка: пустой USER пакет"
                )

                return
            }


            // ==================================
            // СОЗДАЁМ BUFFER
            // ==================================

            val buffer = ByteBuffer.wrap(
                data
            )


            // ==================================
            // ПРОВЕРЯЕМ ЧТО ЕСТЬ HEADER
            // ==================================

            if (buffer.remaining() < 9) {

                println(
                    "Ошибка: USER пакет слишком короткий"
                )

                return
            }


            // ==================================
            // ЧИТАЕМ AUTH_USER
            // ==================================

            val headerBytes = ByteArray(
                9
            )


            buffer.get(
                headerBytes
            )


            val header = String(
                headerBytes,
                Charsets.UTF_8
            )


            println(
                "Заголовок: $header"
            )


            // ==================================
            // ПРОВЕРЯЕМ HEADER
            // ==================================

            if (header != "AUTH_USER") {

                println(
                    "Неверный тип USER пакета: $header"
                )

                return
            }


            // ==================================
            // UNIC
            // ==================================

            val unic = readLongString(
                buffer
            )


            // ==================================
            // AVA
            // ==================================

            val ava = readLongString(
                buffer
            )


            // ==================================
            // NIK
            // ==================================

            val nik = readLongString(
                buffer
            )


            // ==================================
            // СОХРАНЯЕМ ДАННЫЕ
            // ==================================

            myUnic = unic

            myAva = ava

            myNik = nik


            // ==================================
            // ОТЛАДКА
            // ==================================

            println(
                "Данные текущего пользователя:"
            )

            println(
                "unic = $myUnic"
            )

            println(
                "ava = $myAva"
            )

            println(
                "nik = $myNik"
            )

            println(
                "================================="
            )

        } catch (error: Exception) {

            println(
                "================================="
            )

            println(
                "ОШИБКА ОБРАБОТКИ USER ПАКЕТА"
            )

            println(
                error.toString()
            )

            println(
                "================================="
            )
        }
    }


    // ======================================
    // ПРИНЯТЬ BINARY ПАКЕТ ЧАТА
    //
    // ПЕРВЫЙ БАЙТ:
    // 'c'
    // ======================================

    fun receiveBinary(
        data: ByteArray
    ) {

        try {

            println(
                "================================="
            )

            println(
                "Получен BINARY пакет чата"
            )

            println(
                "Размер: ${data.size} байт"
            )


            // ==================================
            // ПРОВЕРЯЕМ ЧТО ПАКЕТ НЕ ПУСТОЙ
            // ==================================

            if (data.isEmpty()) {

                println(
                    "Ошибка: пустой BINARY пакет"
                )

                return
            }


            // ==================================
            // СОЗДАЁМ BUFFER
            // ==================================

            val buffer = ByteBuffer.wrap(data)


            // ==================================
            // КЛЮЧ ПАКЕТА
            // ==================================

            val packetType =
                buffer.get().toInt().toChar()


            println(
                "Тип пакета: $packetType"
            )


            // ==================================
            // ПРОВЕРЯЕМ КЛЮЧ ЧАТА
            // ==================================

            if (packetType != 'c') {

                println(
                    "Неизвестный тип пакета чата: $packetType"
                )

                return
            }


            // ==================================
            // CHAT_ID
            // ==================================

            val chatId = readString(
                buffer
            )


            // ==================================
            // UNIC_ID
            // ==================================

            val unicId = readString(
                buffer
            )


            // ==================================
            // NICKNAME
            // ==================================

            val nickname = readString(
                buffer
            )


            // ==================================
            // AVA
            // ==================================

            val avaString = readString(
                buffer
            )


            // ==================================
            // AVA В ЧИСЛО
            // ==================================

            val ava =
                avaString.toIntOrNull() ?: 0


            // ==================================
            // СОЗДАЁМ ЧАТ
            // ==================================

            val chat = ServerChat(
                chatId = chatId,
                unicId = unicId,
                nickname = nickname,
                ava = ava
            )


            // ==================================
            // ДОБАВЛЯЕМ ЧАТ
            // ==================================

            chats.add(
                chat
            )


            // ==================================
            // ОТЛАДКА
            // ==================================

            println(
                "Чат добавлен:"
            )

            println(
                "chat_id = $chatId"
            )

            println(
                "unic_id = $unicId"
            )

            println(
                "nickname = $nickname"
            )

            println(
                "ava = $ava"
            )

            println(
                "Всего чатов: ${chats.size}"
            )

            println(
                "================================="
            )

        } catch (error: Exception) {

            println(
                "================================="
            )

            println(
                "ОШИБКА ОБРАБОТКИ BINARY ЧАТА"
            )

            println(
                error.toString()
            )

            println(
                "================================="
            )
        }
    }


    // ======================================
    // ПРИНЯТЬ BINARY ПАКЕТ СООБЩЕНИЯ
    //
    // ПЕРВЫЙ БАЙТ:
    // 'C'
    //
    // ФОРМАТ:
    //
    // C
    // + id_msg
    // + data
    // + time
    // + kto
    // + komu
    // + type
    // + text
    // + chat_id
    //
    // КАЖДАЯ СТРОКА:
    //
    // 1 БАЙТ ДЛИНА
    // + UTF-8 ДАННЫЕ
    // ======================================

    fun receiveMessage(
        data: ByteArray
    ) {

        try {

            println(
                "================================="
            )

            println(
                "Получен BINARY пакет сообщения"
            )

            println(
                "Размер: ${data.size} байт"
            )


            // ==================================
            // ПРОВЕРЯЕМ ЧТО ПАКЕТ НЕ ПУСТОЙ
            // ==================================

            if (data.isEmpty()) {

                println(
                    "Ошибка: пустой BINARY пакет сообщения"
                )

                return
            }


            // ==================================
            // СОЗДАЁМ BUFFER
            // ==================================

            val buffer = ByteBuffer.wrap(data)


            // ==================================
            // КЛЮЧ ПАКЕТА
            // ==================================

            val packetType =
                buffer.get().toInt().toChar()


            println(
                "Тип пакета сообщения: $packetType"
            )


            // ==================================
            // ПРОВЕРЯЕМ КЛЮЧ СООБЩЕНИЯ
            // ==================================

            if (packetType != 'C') {

                println(
                    "Неизвестный тип пакета сообщения: $packetType"
                )

                return
            }


            // ==================================
            // ID_MSG
            // ==================================

            val idMsg = readString(
                buffer
            )


            // ==================================
            // DATA
            // ==================================

            val dataValue = readString(
                buffer
            )


            // ==================================
            // TIME
            // ==================================

            val time = readString(
                buffer
            )


            // ==================================
            // KTO
            // ==================================

            val kto = readString(
                buffer
            )


            // ==================================
            // KOMU
            // ==================================

            val komu = readString(
                buffer
            )


            // ==================================
            // TYPE
            // ==================================

            val type = readString(
                buffer
            )


            // ==================================
            // TEXT
            // ==================================

            val text = readString(
                buffer
            )


            // ==================================
            // CHAT_ID
            // ==================================

            val chatId = readString(
                buffer
            )


            // ==================================
            // СОЗДАЁМ СООБЩЕНИЕ
            // ==================================

            val message = ServerMessage(
                idMsg = idMsg,
                data = dataValue,
                time = time,
                kto = kto,
                komu = komu,
                type = type,
                text = text,
                chatId = chatId
            )


            // ==================================
            // ДОБАВЛЯЕМ СООБЩЕНИЕ
            // ==================================

            messages.add(
                message
            )


            // ==================================
            // ОТЛАДКА
            // ==================================

            println(
                "Сообщение добавлено:"
            )

            println(
                "id_msg = $idMsg"
            )

            println(
                "data = $dataValue"
            )

            println(
                "time = $time"
            )

            println(
                "kto = $kto"
            )

            println(
                "komu = $komu"
            )

            println(
                "type = $type"
            )

            println(
                "text = $text"
            )

            println(
                "chat_id = $chatId"
            )

            println(
                "Всего сообщений: ${messages.size}"
            )

            println(
                "================================="
            )

        } catch (error: Exception) {

            println(
                "================================="
            )

            println(
                "ОШИБКА ОБРАБОТКИ СООБЩЕНИЯ"
            )

            println(
                error.toString()
            )

            println(
                error
            )

            println(
                "================================="
            )
        }
    }


    // ======================================
    // ЧТЕНИЕ СТРОКИ ИЗ BINARY
    //
    // ФОРМАТ:
    //
    // [ДЛИНА 1 БАЙТ]
    // [UTF-8 ДАННЫЕ]
    // ======================================

    private fun readString(
        buffer: ByteBuffer
    ): String {

        // ==================================
        // ПРОВЕРЯЕМ ЧТО ЕСТЬ БАЙТ ДЛИНЫ
        // ==================================

        if (!buffer.hasRemaining()) {

            throw IllegalArgumentException(
                "Неожиданный конец пакета при чтении длины строки"
            )
        }


        // ==================================
        // ЧИТАЕМ ДЛИНУ
        // ==================================

        val length =
            buffer.get().toInt() and 0xFF


        // ==================================
        // ПРОВЕРЯЕМ ЧТО В BUFFER
        // ДОСТАТОЧНО ДАННЫХ
        // ==================================

        if (buffer.remaining() < length) {

            throw IllegalArgumentException(
                "Недостаточно данных для чтения строки. " +
                        "Нужно: $length байт, " +
                        "доступно: ${buffer.remaining()} байт"
            )
        }


        // ==================================
        // ЧИТАЕМ BYTES
        // ==================================

        val bytes =
            ByteArray(length)


        buffer.get(
            bytes
        )


        // ==================================
        // UTF-8
        // ==================================

        return String(
            bytes,
            Charsets.UTF_8
        )
    }


    // ======================================
    // ЧТЕНИЕ СТРОКИ С ДЛИНОЙ 2 БАЙТА
    //
    // ФОРМАТ:
    //
    // [ДЛИНА 2 БАЙТА]
    // [UTF-8 ДАННЫЕ]
    // ======================================

    private fun readLongString(
        buffer: ByteBuffer
    ): String {

        // ==================================
        // ПРОВЕРЯЕМ ЧТО ЕСТЬ 2 БАЙТА ДЛИНЫ
        // ==================================

        if (buffer.remaining() < 2) {

            throw IllegalArgumentException(
                "Неожиданный конец пакета при чтении длины строки"
            )
        }


        // ==================================
        // ЧИТАЕМ ДЛИНУ
        // ==================================

        val length =
            buffer.short.toInt() and 0xFFFF


        // ==================================
        // ПРОВЕРЯЕМ ЧТО В BUFFER
        // ДОСТАТОЧНО ДАННЫХ
        // ==================================

        if (buffer.remaining() < length) {

            throw IllegalArgumentException(
                "Недостаточно данных для чтения строки. " +
                        "Нужно: $length байт, " +
                        "доступно: ${buffer.remaining()} байт"
            )
        }



        // ==================================
        // ЧИТАЕМ BYTES
        // ==================================

        val bytes =
            ByteArray(length)


        buffer.get(
            bytes
        )


        // ==================================
        // UTF-8
        // ==================================

        return String(
            bytes,
            Charsets.UTF_8
        )
    }


    // ======================================
    // ЗАКРЫТЬ СОЕДИНЕНИЕ
    // ======================================


    fun disconnect() {

        webSocket?.close(
            1000,
            "Client disconnected"
        )

        webSocket = null

        chats.clear()

        messages.clear()

        myUnic = null

        myAva = null

        myNik = null
    }
}



