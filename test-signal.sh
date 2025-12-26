#!/bin/bash
# Скрипт для отправки тестовых сигналов светофору

if [ -z "$1" ]; then
    echo "Использование: $0 [RED_BLINK|YELLOW_BLINK|GREEN]"
    echo ""
    echo "Примеры:"
    echo "  ./test-signal.sh RED_BLINK     # Красный мигающий (120 сек)"
    echo "  ./test-signal.sh YELLOW_BLINK  # Желтый мигающий (120 сек)"
    echo "  ./test-signal.sh GREEN         # Зеленый постоянный"
    exit 1
fi

SIGNAL=$1
echo "Отправка сигнала: $SIGNAL"

# Отправляем через netcat
echo "$SIGNAL" | nc localhost 52521

echo "Сигнал отправлен!"
