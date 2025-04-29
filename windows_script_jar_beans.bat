@echo off
setlocal

:: === CONFIGURAZIONE ===
set BASE_DIR=out\production\The_Matching_Pairs_Game
set JAR_DIR=beans_jars
set TEMP_MANIFEST=__temp_manifest.mf

:: Crea la cartella dei JAR se non esiste
if not exist %JAR_DIR% mkdir %JAR_DIR%

:: Ciclo sui JavaBeans da impacchettare
for %%B in (Board Card Challenge Controller Counter) do (

    echo Creazione %%B.jar...

    :: 1. Crea manifest temporaneo
    > %TEMP_MANIFEST% echo Manifest-Version: 1.0
    >> %TEMP_MANIFEST% echo Java-Bean: True
    >> %TEMP_MANIFEST% echo.

    :: 2. Crea il JAR con manifest e classe principale
    jar cfm %JAR_DIR%\%%B.jar %TEMP_MANIFEST% -C %BASE_DIR% %%B.class

    :: 3. Aggiunge tutte le inner class (es. Card$1.class)
    for %%F in (%BASE_DIR%\%%B$*.class) do (
        jar uf %JAR_DIR%\%%B.jar -C %BASE_DIR% %%~nxF
    )
)

:: 4. Pulisce il manifest temporaneo
del %TEMP_MANIFEST%

echo Tutti i JAR dei bean sono stati creati in %JAR_DIR%
pause
