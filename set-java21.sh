#!/usr/bin/env bash
JAVA_21_HOME="$(/usr/libexec/java_home -v 21 2>/dev/null || true)"
if [ -n "$JAVA_21_HOME" ]; then
  export JAVA_HOME="$JAVA_21_HOME"
  export PATH="$JAVA_HOME/bin:$PATH"
  echo "JAVA_HOME set to $JAVA_HOME"
else
  echo "No Java 21 installation detected. See README.md for install instructions."
fi
