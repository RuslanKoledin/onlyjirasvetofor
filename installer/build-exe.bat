@echo off
echo ================================================
echo Svetoofor EXE Installer Builder
echo ================================================
echo.

REM Check if launch4j exists
if not exist "C:\Program Files (x86)\Launch4j\launch4jc.exe" (
    if not exist "C:\Program Files\Launch4j\launch4jc.exe" (
        echo ERROR: Launch4j not found!
        echo Please download and install Launch4j from:
        echo https://launch4j.sourceforge.net/
        echo.
        pause
        exit /b 1
    )
    set LAUNCH4J="C:\Program Files\Launch4j\launch4jc.exe"
) else (
    set LAUNCH4J="C:\Program Files (x86)\Launch4j\launch4jc.exe"
)

echo Found Launch4j: %LAUNCH4J%
echo.

REM Check if JAR exists
if not exist "svetoofor-installer.jar" (
    echo ERROR: svetoofor-installer.jar not found!
    echo Please copy the JAR file to this directory.
    echo.
    pause
    exit /b 1
)

echo Building EXE installer...
%LAUNCH4J% launch4j-config.xml

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ================================================
    echo SUCCESS! EXE installer created:
    echo SvetooforInstaller.exe
    echo ================================================
    echo.
    echo You can now distribute SvetooforInstaller.exe
    echo to users. They need Java 17+ installed.
    echo.
) else (
    echo.
    echo ERROR: Failed to create EXE installer!
    echo Check the error messages above.
    echo.
)

pause
