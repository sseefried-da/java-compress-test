#!/bin/bash

#
# This script will use jmap to dump the heap from a running Java JAR
#
# Usage: java-heap-dump.sh <regex>
#
# The <regex> must match a running .jar file in the output of `jps -l`
#
# The dump file will be dumped to DUMP_DIR and have the format
#  <basename of jar>.%Y-%m-%d-%H-%M-%S.hprof.gz
#
#  e.g. ftfw.2021-04-16-16-15-23.hprof.gz
#
# The output can be analysed with tools like Yourkit
# (https://www.yourkit.com/java/profiler/features/)

THIS_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
BASE_DIR="$( cd "$THIS_DIR" && pwd )"

## Configuration

# Keep the last NUM_DUMP_FILES_TO_KEEP dump files
NUM_DUMP_FILES_TO_KEEP=4

# Heap dump directory
DUMP_DIR=$BASE_DIR/heap-dumps

##############

if [[ "${CI}" = 'true' ]]; then export PATH="$PATH:${JAVA_HOME}/bin"; fi

function base_dump_name() {
    echo "$DUMP_DIR/$(basename $1 .jar).dump"
}

function filename() {
    echo "$(base_dump_name $1).$(date +%Y-%m-%d-%H-%M-%S).hprof"
}

function delete_old_gzipped_dumps() {
  TMP=$(mktemp  dump_files.XXXXX)
  ls $(base_dump_name $1)*hprof.gz | sort > "$TMP"
  N=$(cat $TMP | wc -l)
  if [ "$(expr $N \> $NUM_DUMP_FILES_TO_KEEP)" = "1" ]; then
    head -n $(expr $N - $NUM_DUMP_FILES_TO_KEEP) "$TMP" | xargs rm
  fi
  rm -f "$TMP"
}

#########################

[[ $# -lt 1 ]] && { echo "Usage: $(basename $0) <running jar>"; exit 1;}

LINE=$(jps -l | grep "$1")
[[ "$LINE" = "" ]] && { echo "Jar matching '$1' is not running. Running JARs are:"; jps -l | grep '\.jar'; exit 1; }

PID="$(echo $LINE | awk '{ print $1 }' )"
JAR_BASE="$(basename $(echo $LINE | awk '{ print $2 }') .jar)"

FILENAME=$(filename "$JAR_BASE")

mkdir -p "$DUMP_DIR"
time jmap -dump:live,format=b,file="$FILENAME" "$PID"
ls -lh "$FILENAME"
echo "Gzipping $FILENAME"
time gzip -1 "$FILENAME"
ls -lh "$FILENAME.gz"
delete_old_gzipped_dumps "$JAR_BASE"