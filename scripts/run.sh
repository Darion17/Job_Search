#!/usr/bin/env bash
set -euo pipefail
# scripts/run.sh - compile and run Scraper.java from repo root

REPO_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
LIBS_DIR="$REPO_ROOT/libs"
JSOUP_JAR="$LIBS_DIR/jsoup-1.16.1.jar"

mkdir -p "$LIBS_DIR"
if [ ! -f "$JSOUP_JAR" ]; then
  echo "Downloading jsoup..."
  curl -L -o "$JSOUP_JAR" "https://repo1.maven.org/maven2/org/jsoup/jsoup/1.16.1/jsoup-1.16.1.jar"
fi

echo "Compiling Scraper.java..."
javac -cp "$JSOUP_JAR" "$REPO_ROOT/Scraper.java"

echo "Running Scraper (usage output)..."
java -cp ".:$JSOUP_JAR" Scraper
