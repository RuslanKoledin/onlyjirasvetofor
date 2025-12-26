#!/bin/bash
# Скрипт для создания Windows EXE installer из JAR файла
# Использует launch4j для конвертации JAR в EXE

set -e

echo "=== Traffic Light Installer - EXE Builder ==="
echo ""

# Проверка что мы в правильной папке
if [ ! -f "launch4j-config.xml" ]; then
    echo "Ошибка: Запустите скрипт из папки installer/"
    exit 1
fi

# Проверка что JAR файл существует
if [ ! -f "../target/svetoofor-installer.jar" ]; then
    echo "Ошибка: Файл ../target/svetoofor-installer.jar не найден"
    echo "Сначала соберите проект: mvn clean package"
    exit 1
fi

# Проверка наличия launch4j
LAUNCH4J=""
if command -v launch4j &> /dev/null; then
    LAUNCH4J="launch4j"
elif [ -f "/usr/local/bin/launch4j" ]; then
    LAUNCH4J="/usr/local/bin/launch4j"
elif [ -f "/opt/launch4j/launch4j" ]; then
    LAUNCH4J="/opt/launch4j/launch4j"
elif [ -d "/Applications/launch4j.app" ]; then
    LAUNCH4J="/Applications/launch4j.app/Contents/MacOS/launch4j"
else
    echo "⚠️  Launch4j не найден"
    echo ""
    echo "Установите launch4j:"
    echo ""
    echo "macOS (Homebrew):"
    echo "  brew install launch4j"
    echo ""
    echo "Linux (Ubuntu/Debian):"
    echo "  sudo apt-get install launch4j"
    echo ""
    echo "Windows:"
    echo "  Скачайте с https://sourceforge.net/projects/launch4j/"
    echo "  Или используйте build-exe.bat скрипт"
    echo ""
    exit 1
fi

echo "✅ Launch4j найден: $LAUNCH4J"
echo "✅ JAR файл найден: ../target/svetoofor-installer.jar"
echo ""
echo "Создание EXE файла..."

# Запускаем launch4j
"$LAUNCH4J" launch4j-config.xml

if [ -f "svetoofor-installer.exe" ]; then
    echo ""
    echo "✅ EXE файл успешно создан!"
    echo ""
    echo "📦 Файл готов к распространению:"
    echo "   $(pwd)/svetoofor-installer.exe"
    echo ""
    ls -lh svetoofor-installer.exe
    echo ""
    echo "Теперь вы можете скопировать этот EXE на Windows компьютеры"
    echo "и запустить установку светофора."
else
    echo ""
    echo "❌ Ошибка: EXE файл не был создан"
    exit 1
fi
