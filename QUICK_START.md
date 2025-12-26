# 🚀 Быстрый старт на рабочем ПК

## ✅ Порт изменён: 8887 → 52521

Все файлы обновлены. Теперь сервер работает на порту **52521**.

---

## Вариант 1: Запуск на Windows (Рабочий ПК с доступом к JIRA)

### Шаг 1: Скопируйте файлы с Mac на рабочий ПК

**На Mac:**
```bash
# Создайте архив
cd /Users/ruslan/onlyjirasvetofor
tar -czf svetoofor-deploy.tar.gz target/svetoofor-server.jar server.properties.example deploy/env.example

# Скопируйте на рабочий ПК через USB/email/сетевую папку
```

**Или через SSH (если есть доступ):**
```bash
scp svetoofor-deploy.tar.gz username@work-pc:/path/to/destination/
```

### Шаг 2: Распакуйте на Windows

```cmd
# Распакуйте архив (используйте 7-Zip или WinRAR)
# Или через WSL/Git Bash:
tar -xzf svetoofor-deploy.tar.gz
```

### Шаг 3: Создайте файл с учетными данными

**Создайте файл `.env` рядом с `svetoofor-server.jar`:**

```bash
# Содержимое .env файла:
JIRA_URL=https://jiraportal.cbk.kg
JIRA_USERNAME=incident
JIRA_PASSWORD=qweasd123#$
```

### Шаг 4: Запустите сервер

**Вариант A: Через CMD (простой способ)**
```cmd
cd path\to\svetoofor
set JIRA_URL=https://jiraportal.cbk.kg
set JIRA_USERNAME=incident
set JIRA_PASSWORD=qweasd123#$
java -jar svetoofor-server.jar
```

**Вариант B: Через PowerShell**
```powershell
cd path\to\svetoofor
$env:JIRA_URL="https://jiraportal.cbk.kg"
$env:JIRA_USERNAME="incident"
$env:JIRA_PASSWORD="qweasd123#$"
java -jar svetoofor-server.jar
```

**Вариант C: Через .bat скрипт**

Создайте файл `start-server.bat`:
```batch
@echo off
set JIRA_URL=https://jiraportal.cbk.kg
set JIRA_USERNAME=incident
set JIRA_PASSWORD=qweasd123#$
java -jar svetoofor-server.jar
pause
```

Запустите двойным кликом на `start-server.bat`

---

## Вариант 2: Запуск на Linux (Сервер в корпоративной сети)

### Шаг 1: Скопируйте на сервер

```bash
# На Mac
cd /Users/ruslan/onlyjirasvetofor
tar -czf svetoofor.tar.gz target/svetoofor-server.jar server.properties deploy/
scp svetoofor.tar.gz user@linux-server:/tmp/

# На Linux сервере
ssh user@linux-server
cd /tmp
tar -xzf svetoofor.tar.gz
```

### Шаг 2: Установите как сервис

```bash
cd deploy
sudo ./install.sh

# Настройте учетные данные
sudo nano /etc/systemd/system/svetoofor.service
# Измените JIRA_PASSWORD

# Запустите
sudo systemctl daemon-reload
sudo systemctl enable svetoofor
sudo systemctl start svetoofor
sudo systemctl status svetoofor
```

---

## 🔍 Проверка работы

### После запуска вы должны увидеть:

```
=== Traffic Light Server (JIRA Only) ===
Loaded server.properties
WebSocket server started on port 52521
Starting JIRA integration...
JIRA URL: https://jiraportal.cbk.kg
JIRA Username: incident
Poll interval: 5 minutes
Server started on port 52521
Запуск JIRA Poller...
Интервал опроса: 5 минут
Тип инцидента: 11206
✅ Подключение к JIRA успешно

🔍 Опрос JIRA на наличие новых инцидентов...
   Новых инцидентов не найдено
```

### Проверка подключения к JIRA (на рабочем ПК)

```bash
# Проверьте доступ к JIRA
curl -u incident:qweasd123#$ https://jiraportal.cbk.kg/rest/api/2/myself

# Если работает - увидите JSON с вашим профилем
```

### Проверка WebSocket порта

```bash
# Windows (PowerShell)
Test-NetConnection -ComputerName localhost -Port 52521

# Linux/Mac
nc -zv localhost 52521
netstat -an | grep 52521
```

---

## 🖥️ Запуск клиента-светофора

После того как сервер запущен на рабочем ПК или сервере:

### Вариант 1: Windows EXE Установщик (Рекомендуется)

Для удобства пользователей используйте **EXE установщик**:

1. Перейдите в папку `installer/`
2. Прочитайте `QUICK_START_RU.txt` или `README.md`
3. Создайте `SvetooforInstaller.exe` с помощью `build-exe.bat`
4. Раздайте EXE файл пользователям

**Преимущества:**
- ✅ Двойной клик для установки (не нужно java -jar)
- ✅ Графический интерфейс
- ✅ Автоматическое добавление в автозагрузку

**Подробнее**: см. `installer/README.md`

### Вариант 2: Запуск JAR вручную

**На Windows:**

```cmd
# Отредактируйте client.properties
server.address=localhost
server.port=52521

# Запустите клиент
java -jar svetoofor-client.jar
```

**На других компьютерах:**

```cmd
# Отредактируйте client.properties
server.address=192.168.1.100  # IP адрес сервера
server.port=52521

# Запустите клиент
java -jar svetoofor-client.jar
```

---

## ❌ Решение проблем

### Ошибка: "Connect timed out" к JIRA

**Причина**: Нет доступа к JIRA с текущего компьютера

**Решение**:
1. Убедитесь что запускаете на компьютере в корпоративной сети CBK
2. Проверьте VPN подключение (если требуется)
3. Проверьте доступность: `curl https://jiraportal.cbk.kg`

### Ошибка: "Address already in use"

**Причина**: Порт 52521 уже занят

**Решение**:
```bash
# Windows
netstat -ano | findstr :52521
taskkill /PID <номер_процесса> /F

# Linux
lsof -i :52521
kill -9 <PID>
```

### Ошибка: "JIRA URL cannot be null or empty"

**Причина**: Переменные окружения не установлены

**Решение**: Убедитесь что вы установили переменные окружения перед запуском:
```bash
export JIRA_URL="https://jiraportal.cbk.kg"
export JIRA_USERNAME="incident"
export JIRA_PASSWORD="qweasd123#$"
```

---

## 📌 Важно

1. ✅ Порт изменён с **8887** на **52521** во всех файлах
2. ✅ Сервер должен иметь доступ к https://jiraportal.cbk.kg
3. ✅ Клиенты подключаются к серверу на порту **52521**
4. ✅ Firewall должен разрешать порт **52521** (если включён)

---

## 🎯 Готовые команды для копипаста

### Windows (CMD) - Запуск сервера
```cmd
set JIRA_URL=https://jiraportal.cbk.kg && set JIRA_USERNAME=incident && set JIRA_PASSWORD=qweasd123#$ && java -jar svetoofor-server.jar
```

### Linux/Mac - Запуск сервера
```bash
export JIRA_URL="https://jiraportal.cbk.kg" && export JIRA_USERNAME="incident" && export JIRA_PASSWORD="qweasd123#$" && java -jar target/svetoofor-server.jar
```

---

**Успешного запуска! 🚀**

Если возникнут проблемы, смотрите подробную документацию:
- `DEPLOYMENT.md` - инструкция по деплою
- `SECURITY.md` - безопасность
- `deploy/README.md` - детальные инструкции
