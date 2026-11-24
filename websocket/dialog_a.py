import struct
from datetime import datetime

import dialog_hub


# ==========================================
# ПРИЁМ ПАКЕТА ОТ КЛИЕНТА
# ==========================================

async def receive_packet(
    websocket,
    packet
):

    # ======================================
    # ПРОВЕРЯЕМ BINARY
    # ======================================

    if not isinstance(
        packet,
        bytes
    ):

        print(
            "Пакет отклонён: не binary"
        )

        return


    # ======================================
    # ПЕРВЫЙ БАЙТ
    # ======================================

    if len(packet) < 1:

        print(
            "Пакет отклонён: пустой пакет"
        )

        return


    if packet[0:1] != b"a":

        print(
            "Пакет отклонён: неизвестный тип"
        )

        return


    position = 1


    # ======================================
    # UNIC ОТПРАВИТЕЛЯ
    # ======================================

    if position >= len(packet):

        print(
            "Пакет отклонён: отсутствует unic отправителя"
        )

        return


    sender_length = packet[position]

    position += 1


    if sender_length == 0:

        print(
            "Пакет отклонён: unic отправителя пустой"
        )

        return


    if position + sender_length > len(packet):

        print(
            "Пакет отклонён: повреждён unic отправителя"
        )

        return


    try:

        sender_unic = packet[
            position:
            position + sender_length
        ].decode("utf-8")

    except UnicodeDecodeError:

        print(
            "Пакет отклонён: неверный UTF-8 unic отправителя"
        )

        return


    position += sender_length


    # ======================================
    # UNIC ПОЛУЧАТЕЛЯ
    # ======================================

    if position >= len(packet):

        print(
            "Пакет отклонён: отсутствует unic получателя"
        )

        return


    recipient_length = packet[position]

    position += 1


    if recipient_length == 0:

        print(
            "Пакет отклонён: unic получателя пустой"
        )

        return


    if position + recipient_length > len(packet):

        print(
            "Пакет отклонён: повреждён unic получателя"
        )

        return


    try:

        recipient_unic = packet[
            position:
            position + recipient_length
        ].decode("utf-8")

    except UnicodeDecodeError:

        print(
            "Пакет отклонён: неверный UTF-8 unic получателя"
        )

        return


    position += recipient_length


    # ======================================
    # CHAT ID
    # ======================================

    if position >= len(packet):

        print(
            "Пакет отклонён: отсутствует chat_id"
        )

        return


    chat_id_length = packet[position]

    position += 1


    if chat_id_length == 0:

        print(
            "Пакет отклонён: chat_id пустой"
        )

        return


    if position + chat_id_length > len(packet):

        print(
            "Пакет отклонён: повреждён chat_id"
        )

        return


    try:

        chat_id = packet[
            position:
            position + chat_id_length
        ].decode("utf-8")

    except UnicodeDecodeError:

        print(
            "Пакет отклонён: неверный UTF-8 chat_id"
        )

        return


    position += chat_id_length


    # ======================================
    # ДЛИНА ТЕКСТА
    # ======================================

    if position + 2 > len(packet):

        print(
            "Пакет отклонён: отсутствует длина текста"
        )

        return


    text_length = struct.unpack(
        "!H",
        packet[
            position:
            position + 2
        ]
    )[0]


    position += 2


    # ======================================
    # ПРОВЕРКА РАЗМЕРА ТЕКСТА
    # ======================================

    if text_length == 0:

        print(
            "Пакет отклонён: сообщение пустое"
        )

        return


    if position + text_length + 1 > len(packet):

        print(
            "Пакет отклонён: повреждён текст"
        )

        return


    # ======================================
    # ТЕКСТ
    # ======================================

    try:

        text = packet[
            position:
            position + text_length
        ].decode("utf-8")

    except UnicodeDecodeError:

        print(
            "Пакет отклонён: неверный UTF-8 текст"
        )

        return


    position += text_length


    # ======================================
    # ПРОВЕРЯЕМ ПУСТОЙ ТЕКСТ
    # ======================================

    if text.strip() == "":

        print(
            "Пакет отклонён: сообщение состоит только из пробелов"
        )

        return


    # ======================================
    # МАКСИМУМ 3000 СИМВОЛОВ
    # ======================================

    if len(text) > 3000:

        print(
            "Пакет отклонён: сообщение длиннее 3000 символов"
        )

        return


    # ======================================
    # TYPE
    # ======================================

    if position >= len(packet):

        print(
            "Пакет отклонён: отсутствует type"
        )

        return


    message_type = packet[position]

    position += 1


    # ======================================
    # ПОКА РАЗРЕШЁН ТОЛЬКО TYPE 0
    # ======================================

    if message_type != 0:

        print(
            "Пакет отклонён: неизвестный type"
        )

        return


    # ======================================
    # ЛИШНИЕ ДАННЫЕ
    # ======================================

    if position != len(packet):

        print(
            "Пакет отклонён: лишние данные в конце"
        )

        return


    # ======================================
    # ПРОВЕРЯЕМ ОТПРАВИТЕЛЯ В USERS
    # ======================================

    sender_user = None


    for user in dialog_hub.users:

        if user["unic_id"] == sender_unic:

            sender_user = user

            break


    if sender_user is None:

        print(
            "Пакет отклонён: отправитель отсутствует в users"
        )

        return


    # ======================================
    # ПРОВЕРЯЕМ СОЕДИНЕНИЕ В ONLINE
    # ======================================

    connection_user = None


    for online_user in dialog_hub.online:

        if online_user["websocket"] is websocket:

            connection_user = online_user

            break


    if connection_user is None:

        print(
            "Пакет отклонён: соединение отсутствует в online"
        )

        return


    # ======================================
    # ПРОВЕРЯЕМ ЧТО UNIC ПРИНАДЛЕЖИТ
    # ЭТОМУ WEBSOCKET
    # ======================================

    if connection_user["unic_id"] != sender_unic:

        print(
            "Пакет отклонён: unic не принадлежит соединению"
        )

        return


    # ==========================================
    # ВСЕ ПРОВЕРКИ ПРОЙДЕНЫ
    # ==========================================


    # ==========================================
    # ПОЛУЧАЕМ НОВЫЙ ID СООБЩЕНИЯ
    # ==========================================

    new_id = dialog_hub.msg_count + 1


    # ==========================================
    # ТЕКУЩИЕ ДАТА И ВРЕМЯ
    # ==========================================

    now = datetime.now()

    message_data = now.date()

    message_time = now.time()


    # ==========================================
    # СОЗДАЁМ ЗАПИСЬ СООБЩЕНИЯ
    # ==========================================

    new_message = {

        "id_msg": new_id,

        "data": message_data,

        "time": message_time,

        "kto": sender_unic,

        "komu": recipient_unic,

        "type": message_type,

        "text": text,

        "chat_id": chat_id

    }


    # ==========================================
    # ЗАПИСЫВАЕМ СООБЩЕНИЕ В DATABASE
    # ==========================================

    try:

        cursor = dialog_hub.db.cursor()


        cursor.execute(
            """
            INSERT INTO msg1026
            (
                id_msg,
                data,
                time,
                kto,
                komu,
                type,
                text,
                chat_id
            )
            VALUES
            (
                %s,
                %s,
                %s,
                %s,
                %s,
                %s,
                %s,
                %s
            )
            """,
            (
                new_message["id_msg"],
                new_message["data"],
                new_message["time"],
                new_message["kto"],
                new_message["komu"],
                new_message["type"],
                new_message["text"],
                new_message["chat_id"]
            )
        )


        dialog_hub.db.commit()


        cursor.close()


    except Exception as error:

        try:

            dialog_hub.db.rollback()

        except Exception:

            pass


        print(
            "Ошибка записи сообщения в database:"
        )

        print(
            error
        )

        return


    # ==========================================
    # ОБНОВЛЯЕМ MSG_COUNT
    # ==========================================

    dialog_hub.msg_count = new_id


    # ==========================================
    # ДОБАВЛЯЕМ СООБЩЕНИЕ В GLOBAL MESSAGES
    # ==========================================

    dialog_hub.messages.append(
        new_message
    )


    # ==========================================
    # СОЗДАЁМ ПАКЕТ C
    #
    # C
    # + id_msg
    # + data
    # + time
    # + kto
    # + komu
    # + type
    # + text
    # + chat_id
    #
    # КАЖДОЕ ПОЛЕ:
    #
    # 1 БАЙТ ДЛИНЫ
    # + ДАННЫЕ
    # ==========================================

    id_msg_bytes = str(
        new_message["id_msg"]
    ).encode("utf-8")


    data_bytes = str(
        new_message["data"]
    ).encode("utf-8")


    time_bytes = str(
        new_message["time"]
    ).encode("utf-8")


    kto_bytes = str(
        new_message["kto"]
    ).encode("utf-8")


    komu_bytes = str(
        new_message["komu"]
    ).encode("utf-8")


    type_bytes = str(
        new_message["type"]
    ).encode("utf-8")


    text_bytes = str(
        new_message["text"]
    ).encode("utf-8")


    chat_id_bytes = str(
        new_message["chat_id"]
    ).encode("utf-8")


    fields = [

        id_msg_bytes,
        data_bytes,
        time_bytes,
        kto_bytes,
        komu_bytes,
        type_bytes,
        text_bytes,
        chat_id_bytes

    ]


    # ==========================================
    # ПРОВЕРЯЕМ ЧТО ПОЛЯ ПОМЕСТЯТСЯ
    # В 1 БАЙТ ДЛИНЫ
    # ==========================================

    for field in fields:

        if len(field) > 255:

            print(
                "Ошибка: поле сообщения слишком длинное"
            )

            return


    # ==========================================
    # СОБИРАЕМ C
    # ==========================================

    response_packet = b"C"


    for field in fields:

        response_packet += struct.pack(
            "!B",
            len(field)
        )

        response_packet += field


    # ==========================================
    # ОПРЕДЕЛЯЕМ КТО СЕЙЧАС ONLINE
    # ==========================================

    recipients = []


    for online_user in dialog_hub.online:

        online_unic = online_user["unic_id"]


        if (
            online_unic == sender_unic
            or
            online_unic == recipient_unic
        ):

            if online_user["websocket"] not in recipients:

                recipients.append(
                    online_user["websocket"]
                )


    # ==========================================
    # ОТПРАВЛЯЕМ C ONLINE ПОЛЬЗОВАТЕЛЯМ
    # ==========================================

    for online_websocket in recipients:

        try:

            await online_websocket.send(
                response_packet
            )

        except Exception as error:

            print(
                "Ошибка отправки сообщения:"
            )

            print(
                error
            )
