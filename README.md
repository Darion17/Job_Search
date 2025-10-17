# Job_Search — Java 21 upgrade notes

This repository contains a small Java scraper (`Scraper.java`) and helper files to make it easy to run with Java 21 locally on macOS.

Summary
- How to install JDK 21 (Homebrew or SDKMAN)
- How to set `JAVA_HOME` for zsh
- How to build and run (Maven and helper scripts)

Install JDK 21 (macOS)

Homebrew (recommended):

```bash
brew update
brew install --cask temurin@21
java -version
javac -version
```

SDKMAN (alternate):

```bash
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk install java 21-tem
sdk use java 21-tem
```

Set JAVA_HOME (zsh)

In `~/.zshrc`:

```bash
export JAVA_HOME="$(/usr/libexec/java_home -v 21 2>/dev/null || echo "")"
if [ -n "$JAVA_HOME" ]; then
  export PATH="$JAVA_HOME/bin:$PATH"
fi
```

Helper script
- `set-java21.sh`: source it to set JAVA_HOME for the current session.
- `scripts/run.sh`: compiles and runs `Scraper.java` from repo root and downloads jsoup if missing.

Build & run

Option A — manual compile/run (no Maven):

```bash
mkdir -p libs
curl -L -o libs/jsoup.jar \
  https://repo1.maven.org/maven2/org/jsoup/jsoup/1.16.1/jsoup-1.16.1.jar
javac -cp libs/jsoup.jar Scraper.java
java -cp .:libs/jsoup.jar Scraper --xula
```

Option B — helper script:

```bash
chmod +x scripts/run.sh
scripts/run.sh
```

Maven
- A minimal `pom.xml` is included to support building with Java 21 and manage the jsoup dependency.

Notes
- The helper scripts compile the file in-place; if you want the project fully mavenized I can move sources into `src/main/java` and add packaging instructions.
