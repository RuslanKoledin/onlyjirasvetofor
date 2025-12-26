# Деплой на сервер

## Требования

- Linux сервер (Ubuntu 20.04+ / CentOS 7+)
- Java 17 или выше
- Права root (sudo)
- Доступ к JIRA портал (https://jiraportal.cbk.kg)

## Быстрая установка

### 1. Сборка проекта

На вашей машине разработки:

```bash
cd onlyjirasvetofor
mvn clean package
```

### 2. Копирование на сервер

```bash
# Создайте архив
tar -czf svetoofor.tar.gz target/svetoofor-server.jar server.properties deploy/

# Скопируйте на сервер
scp svetoofor.tar.gz user@your-server:/tmp/
```

### 3. Установка на сервере

```bash
ssh user@your-server

# Распакуйте
cd /tmp
tar -xzf svetoofor.tar.gz

# Запустите установочный скрипт
cd deploy
sudo ./install.sh
```

### 4. Настройка учетных данных JIRA

**ВАЖНО**: Установочный скрипт создаёт systemd service с переменными окружения.
Вы ОБЯЗАТЕЛЬНО должны настроить учетные данные JIRA:

```bash
sudo nano /etc/systemd/system/svetoofor.service
```

Найдите и измените:
```ini
Environment="JIRA_USERNAME=incident"
Environment="JIRA_PASSWORD=CHANGE_ME_PLEASE"
```

На ваши реальные учетные данные:
```ini
Environment="JIRA_USERNAME=incident"
Environment="JIRA_PASSWORD=qweasd123#$"
```

### 5. Запуск сервиса

```bash
sudo systemctl daemon-reload
sudo systemctl enable svetoofor
sudo systemctl start svetoofor
```

### 6. Проверка работы

```bash
# Статус сервиса
sudo systemctl status svetoofor

# Логи в реальном времени
sudo journalctl -u svetoofor -f

# Проверка порта
netstat -tulpn | grep 52521
```

## Альтернативный способ - Переменные окружения

Если вы хотите запустить сервер вручную (не через systemd):

```bash
# 1. Создайте файл с переменными окружения
cd /opt/svetoofor
cp /path/to/deploy/env.example .env

# 2. Отредактируйте .env
nano .env

# 3. Загрузите переменные и запустите
source .env
java -jar svetoofor-server.jar
```

## Безопасность

### Защита файла конфигурации

```bash
# Только владелец может читать server.properties
sudo chmod 600 /opt/svetoofor/server.properties
sudo chown svetoofor:svetoofor /opt/svetoofor/server.properties
```

### Firewall настройки

Если вы используете firewall, откройте порт для WebSocket:

```bash
# UFW (Ubuntu)
sudo ufw allow 52521/tcp

# firewalld (CentOS/RHEL)
sudo firewall-cmd --permanent --add-port=52521/tcp
sudo firewall-cmd --reload
```

### SSL/TLS через nginx (опционально)

Для production рекомендуется использовать nginx как reverse proxy:

```nginx
server {
    listen 443 ssl;
    server_name your-domain.com;

    ssl_certificate /path/to/cert.pem;
    ssl_certificate_key /path/to/key.pem;

    location / {
        proxy_pass http://localhost:52521;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host $host;
    }
}
```

## Управление сервисом

```bash
# Запуск
sudo systemctl start svetoofor

# Остановка
sudo systemctl stop svetoofor

# Перезапуск
sudo systemctl restart svetoofor

# Автозапуск при загрузке
sudo systemctl enable svetoofor

# Отключить автозапуск
sudo systemctl disable svetoofor

# Просмотр логов
sudo journalctl -u svetoofor -f
sudo journalctl -u svetoofor -n 100
sudo journalctl -u svetoofor --since "1 hour ago"
```

## Обновление

Когда вы хотите обновить сервер:

```bash
# 1. Остановите сервис
sudo systemctl stop svetoofor

# 2. Скопируйте новый JAR
sudo cp new-svetoofor-server.jar /opt/svetoofor/svetoofor-server.jar

# 3. Запустите сервис
sudo systemctl start svetoofor
```

## Удаление

```bash
# Остановите и отключите сервис
sudo systemctl stop svetoofor
sudo systemctl disable svetoofor

# Удалите файлы
sudo rm /etc/systemd/system/svetoofor.service
sudo rm -rf /opt/svetoofor

# Удалите пользователя
sudo userdel svetoofor

# Перезагрузите systemd
sudo systemctl daemon-reload
```

## Проблемы и решения

### Сервер не запускается

```bash
# Проверьте логи
sudo journalctl -u svetoofor -n 50

# Проверьте настройки JIRA
sudo cat /etc/systemd/system/svetoofor.service | grep Environment

# Проверьте права доступа
ls -la /opt/svetoofor/
```

### Не удаётся подключиться к JIRA

```bash
# Проверьте доступность JIRA
curl -v https://jiraportal.cbk.kg

# Проверьте учетные данные
curl -u username:password https://jiraportal.cbk.kg/rest/api/2/myself

# Может потребоваться VPN для доступа к корпоративной сети
```

### Клиенты не подключаются

```bash
# Проверьте что порт открыт
netstat -tulpn | grep 52521

# Проверьте firewall
sudo ufw status
sudo firewall-cmd --list-all

# Проверьте что сервер слушает правильный интерфейс
ss -tlnp | grep 52521
```

## Мониторинг

Для мониторинга состояния сервера вы можете использовать:

```bash
# Создайте скрипт проверки здоровья
cat > /usr/local/bin/check-svetoofor.sh << 'EOF'
#!/bin/bash
if systemctl is-active --quiet svetoofor; then
    echo "OK: Svetoofor is running"
    exit 0
else
    echo "CRITICAL: Svetoofor is not running"
    exit 2
fi
EOF

chmod +x /usr/local/bin/check-svetoofor.sh
```

## Поддержка

Для получения помощи:
- Email: support@incuat.kg
- GitHub Issues (если применимо)
