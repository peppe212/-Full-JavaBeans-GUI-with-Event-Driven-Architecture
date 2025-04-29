#!/bin/bash

# === CONFIGURAZIONE ===
BASE_DIR="out/production/The_Matching_Pairs_Game"
JAR_DIR="beans_jars"
TEMP_MANIFEST="__temp_manifest.mf"

# Crea la cartella dei JAR se non esiste
mkdir -p "$JAR_DIR"

# Lista dei JavaBeans da impacchettare
for B in Board Card Challenge Controller Counter; do
    echo "Creazione $B.jar..."

    # 1. Crea manifest temporaneo
    echo "Manifest-Version: 1.0" > "$TEMP_MANIFEST"
    echo "Java-Bean: True" >> "$TEMP_MANIFEST"
    echo "" >> "$TEMP_MANIFEST"

    # 2. Crea il JAR con il manifest e la classe principale
    jar cfm "$JAR_DIR/$B.jar" "$TEMP_MANIFEST" -C "$BASE_DIR" "$B.class"

    # 3. Aggiunge eventuali inner classes (es. Board$1.class)
    for inner in "$BASE_DIR"/$B\$*.class; do
        if [ -f "$inner" ]; then
            jar uf "$JAR_DIR/$B.jar" -C "$BASE_DIR" "$(basename "$inner")"
        fi
    done
done

# 4. Rimuove il manifest temporaneo
rm -f "$TEMP_MANIFEST"

echo "✅ Tutti i JAR dei bean sono stati creati in $JAR_DIR"
