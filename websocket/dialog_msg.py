import struct

import dialog_hub


# ==========================================
# ЗАГРУЗКА СООБЩЕНИЙ
# ==========================================

def load_messages():

    print(
        "Загрузка сообщений..."
    )


    try:

        cursor = dialog_hub.db.cursor()


        # ==================================
        # ПОЛУЧАЕМ СООБЩЕНИЯ ИЗ DATABASE
        # ==================================

        cursor.execute(
            """
            SELECT
                id_msg,
                data,
                time,
                kto,
                komu,
                type,
                text,
                chat_id
            FROM msg1026
            """
        )


        rows = cursor.fetchall()


        cursor.close()


        # ==================================
        # СОХРАНЯЕМ В GLOBAL MESSAGES
        # ИЗ DIALOG_HUB
        # ==================================

        dialog_hub.messages.clear()

        # ==========================================
        # ОПРЕДЕЛЯЕМ ПОСЛЕДНИЙ ID СООБЩЕНИЯ
        # ==========================================
        
        if rows:

            valid_counts = [
                int(row[0])
                for row in rows
                if row[0] is not None
            ]

            dialog_hub.msg_count = max(valid_counts) if valid_counts else 0

        else:

            dialog_hub.msg_count = 0

        print(
            "[msg] Текущий msg_count:",
            dialog_hub.msg_count
        )

        for row in rows:

            dialog_hub.messages.append(
                {
                    "id_msg": row[0],
                    "data": row[1],
                    "time": row[2],
                    "kto": row[3],
                    "komu": row[4],
                    "type": row[5],
                    "text": row[6],
                    "chat_id": row[7]
                }
            )


        '''
        print(
            "Сообщения загружены:",
            len(dialog_hub.messages)
        )
        '''


        return True


    except Exception as error:

        print(
            "Ошибка загрузки сообщений:"
        )

        print(
            error
        )


        return False


# ==========================================
# ПОЛУЧЕНИЕ СООБЩЕНИЙ ПОЛЬЗОВАТЕЛЯ
# ==========================================

def get_user_messages(
    user_unic
):

    result = []


    # ======================================
    # ПРОВЕРЯЕМ GLOBAL MESSAGES
    # ======================================

    for message in dialog_hub.messages:

        # Пользователь отправитель
        if message["kto"] == user_unic:

            result.append(
                message
            )

            continue


        # Пользователь получатель
        if message["komu"] == user_unic:

            result.append(
                message
            )


    return result


# ==========================================
# СОЗДАНИЕ БИНАРНОГО ПАКЕТА СООБЩЕНИЯ
# ==========================================

def create_message_packet(
    message
):

    id_msg = str(
        message["id_msg"]
    ).encode("utf-8")


    data = str(
        message["data"]
    ).encode("utf-8")


    time = str(
        message["time"]
    ).encode("utf-8")


    kto = str(
        message["kto"]
    ).encode("utf-8")


    komu = str(
        message["komu"]
    ).encode("utf-8")


    message_type = str(
        message["type"]
    ).encode("utf-8")


    text = str(
        message["text"]
    ).encode("utf-8")


    chat_id = str(
        message["chat_id"]
    ).encode("utf-8")


    # ======================================
    # ПРОВЕРЯЕМ РАЗМЕРЫ
    # ======================================

    if len(id_msg) > 255:
        raise ValueError(
            "id_msg слишком длинный"
        )


    if len(data) > 255:
        raise ValueError(
            "data слишком длинный"
        )


    if len(time) > 255:
        raise ValueError(
            "time слишком длинный"
        )


    if len(kto) > 255:
        raise ValueError(
            "kto слишком длинный"
        )


    if len(komu) > 255:
        raise ValueError(
            "komu слишком длинный"
        )


    if len(message_type) > 255:
        raise ValueError(
            "type слишком длинный"
        )


    if len(text) > 255:
        raise ValueError(
            "text слишком длинный"
        )


    if len(chat_id) > 255:
        raise ValueError(
            "chat_id слишком длинный"
        )


    # ======================================
    # КЛЮЧ ПАКЕТА СООБЩЕНИЯ
    # ======================================

    packet = b"C"


    # ======================================
    # ID_MSG
    # ======================================

    packet += struct.pack(
        "!B",
        len(id_msg)
    )

    packet += id_msg


    # ======================================
    # DATA
    # ======================================

    packet += struct.pack(
        "!B",
        len(data)
    )

    packet += data


    # ======================================
    # TIME
    # ======================================

    packet += struct.pack(
        "!B",
        len(time)
    )

    packet += time


    # ======================================
    # KTO
    # ======================================

    packet += struct.pack(
        "!B",
        len(kto)
    )

    packet += kto


    # ======================================
    # KOMU
    # ======================================

    packet += struct.pack(
        "!B",
        len(komu)
    )

    packet += komu


    # ======================================
    # TYPE
    # ======================================

    packet += struct.pack(
        "!B",
        len(message_type)
    )

    packet += message_type


    # ======================================
    # TEXT
    # ======================================

    packet += struct.pack(
        "!B",
        len(text)
    )

    packet += text


    # ======================================
    # CHAT_ID
    # ======================================

    packet += struct.pack(
        "!B",
        len(chat_id)
    )

    packet += chat_id


    return packet


# ==========================================
# ОТПРАВКА СООБЩЕНИЙ ПОЛЬЗОВАТЕЛЮ
# ==========================================

async def send_user_messages(
    websocket,
    user
):

    user_unic = user["unic_id"]


    # ======================================
    # ПОЛУЧАЕМ СООБЩЕНИЯ ПОЛЬЗОВАТЕЛЯ
    # ======================================

    user_messages = get_user_messages(
        user_unic
    )


    # ======================================
    # ОТПРАВЛЯЕМ КАЖДОЕ СООБЩЕНИЕ
    # ОТДЕЛЬНЫМ BINARY
    # ======================================

    for message in user_messages:

        packet = create_message_packet(
            message
        )


        await websocket.send(
            packet
        )

        '''
        print(
            "Сообщение отправлено:",
            user["nik"],
            "|",
            message["id_msg"],
            "|",
            "chat:",
            message["chat_id"]
        )
        '''


    # ======================================
    # ИТОГ
    # ======================================

    print(
        "Всего сообщений отправлено:",
        user["nik"],
        "|",
        len(user_messages)
    )



