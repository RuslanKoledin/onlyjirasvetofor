#!/bin/bash

#######################################################
# Скрипт установки Traffic Light Server на Linux
#######################################################

set -e

echo "=== Traffic Light Server - Installation Script ==="

# Проверка прав root
if [ "$EUID" -ne 0 ]; then
  echo "Пожалуйста, запустите скрипт с правами root (sudo)"
  exit 1
fi

# Настройки
APP_NAME="svetoofor"
APP_DIR="/opt/${APP_NAME}"
SERVICE_FILE="/etc/systemd/system/${APP_NAME}.service"
USER="${APP_NAME}"

# Создаём пользователя если его нет
if ! id -u ${USER} > /dev/null 2>&1; then
    echo "Создание пользователя ${USER}..."
    useradd -r -s /bin/false ${USER}
fi

# Создаём директорию приложения
echo "Создание директории ${APP_DIR}..."
mkdir -p ${APP_DIR}

# Копируем JAR файл
if [ ! -f "../target/svetoofor-server.jar" ]; then
    echo "❌ Ошибка: файл svetoofor-server.jar не найден!"
    echo "Сначала выполните: mvn clean package"
    exit 1
fi

echo "Копирование JAR файла..."
cp ../target/svetoofor-server.jar ${APP_DIR}/

# Копируем конфигурацию
echo "Копирование server.properties..."
cp ../server.properties ${APP_DIR}/

# Устанавливаем права
echo "Настройка прав доступа..."
chown -R ${USER}:${USER} ${APP_DIR}
chmod 755 ${APP_DIR}
chmod 644 ${APP_DIR}/svetoofor-server.jar
chmod 600 ${APP_DIR}/server.properties  # Защита конфигурации

# Создаём systemd service
echo "Создание systemd service..."
cat > ${SERVICE_FILE} << 'EOF'
[Unit]
Description=Traffic Light Server with JIRA Integration
After=network.target

[Service]
Type=simple
User=svetoofor
WorkingDirectory=/opt/svetoofor
ExecStart=/usr/bin/java -jar /opt/svetoofor/svetoofor-server.jar
Restart=always
RestartSec=10

# Переменные окружения для JIRA (ОБЯЗАТЕЛЬНО настройте!)
Environment="JIRA_URL=https://jiraportal.cbk.kg"
Environment="JIRA_USERNAME=incident"
Environment="JIRA_PASSWORD=CHANGE_ME_PLEASE"

# Безопасность
NoNewPrivileges=true
PrivateTmp=true
ProtectSystem=strict
ProtectHome=true
ReadWritePaths=/opt/svetoofor

[Install]
WantedBy=multi-user.target
EOF

echo ""
echo "✅ Установка завершена!"
echo ""
echo "ВАЖНО: Настройте учетные данные JIRA:"
echo "  sudo nano ${SERVICE_FILE}"
echo "  Измените переменные JIRA_USERNAME и JIRA_PASSWORD"
echo ""
echo "Затем запустите сервис:"
echo "  sudo systemctl daemon-reload"
echo "  sudo systemctl enable ${APP_NAME}"
echo "  sudo systemctl start ${APP_NAME}"
echo ""
echo "Проверка статуса:"
echo "  sudo systemctl status ${APP_NAME}"
echo "  sudo journalctl -u ${APP_NAME} -f"
echo ""
