#!/bin/bash
set -e

PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
ANDROID_HOME="${ANDROID_HOME:-/opt/android-sdk}"
BUILD_TOOLS="$ANDROID_HOME/build-tools/34.0.0"
PLATFORM="$ANDROID_HOME/platforms/android-34"
BUILD_DIR="$PROJECT_DIR/build"
GEN_DIR="$BUILD_DIR/gen"
OBJ_DIR="$BUILD_DIR/obj"
STUB_DIR="$BUILD_DIR/stub-classes"
MOD_DIR="$BUILD_DIR/mod-classes"
DEX_DIR="$BUILD_DIR/dex"
APK_NAME="high-contrast-lsposed"

echo "=== High Contrast LSPosed Module Build ==="
echo "ANDROID_HOME: $ANDROID_HOME"

rm -rf "$BUILD_DIR"
mkdir -p "$GEN_DIR" "$OBJ_DIR" "$STUB_DIR" "$MOD_DIR" "$DEX_DIR"

# Step 1: Compile resources with aapt2
echo "[1/7] Compiling resources..."
"$BUILD_TOOLS/aapt2" compile \
    --dir "$PROJECT_DIR/res" \
    -o "$OBJ_DIR/resources.zip"

# Step 2: Link resources and create base APK
echo "[2/7] Linking resources..."
"$BUILD_TOOLS/aapt2" link \
    -o "$BUILD_DIR/$APK_NAME-unaligned.apk" \
    -I "$PLATFORM/android.jar" \
    --manifest "$PROJECT_DIR/AndroidManifest.xml" \
    --java "$GEN_DIR" \
    --auto-add-overlay \
    "$OBJ_DIR/resources.zip"

# Step 3: Compile Xposed API stubs (compile-only, not included in APK)
echo "[3/7] Compiling Xposed API stubs..."
find "$PROJECT_DIR/src/de" -name "*.java" > "$BUILD_DIR/stub-sources.txt"

javac \
    -source 1.8 -target 1.8 \
    -d "$STUB_DIR" \
    -cp "$PLATFORM/android.jar" \
    -bootclasspath "$PLATFORM/android.jar" \
    @"$BUILD_DIR/stub-sources.txt"

# Step 4: Compile module code (exclude stubs)
echo "[4/7] Compiling module code..."
find "$PROJECT_DIR/src/com" "$GEN_DIR" -name "*.java" > "$BUILD_DIR/mod-sources.txt"

javac \
    -source 1.8 -target 1.8 \
    -d "$MOD_DIR" \
    -cp "$PLATFORM/android.jar:$STUB_DIR" \
    -bootclasspath "$PLATFORM/android.jar" \
    @"$BUILD_DIR/mod-sources.txt"

# Step 5: Convert only module classes to DEX
echo "[5/7] Converting to DEX..."
"$BUILD_TOOLS/d8" \
    --lib "$PLATFORM/android.jar" \
    --output "$DEX_DIR" \
    $(find "$MOD_DIR" -name "*.class")

# Step 6: Add classes.dex to APK
echo "[6/7] Adding DEX to APK..."
cp "$BUILD_DIR/$APK_NAME-unaligned.apk" "$BUILD_DIR/$APK_NAME-unsigned.apk"
(cd "$DEX_DIR" && zip -q "$BUILD_DIR/$APK_NAME-unsigned.apk" classes*.dex)

# Step 7: Align and sign
echo "[7/7] Aligning and signing..."
"$BUILD_TOOLS/zipalign" -f -p 4 \
    "$BUILD_DIR/$APK_NAME-unsigned.apk" \
    "$BUILD_DIR/$APK_NAME-aligned.apk"

KEYSTORE="$HOME/.android/debug.keystore"
if [ ! -f "$KEYSTORE" ]; then
    mkdir -p "$HOME/.android"
    keytool -genkey -v \
        -keystore "$KEYSTORE" \
        -storepass android \
        -alias androiddebugkey \
        -keypass android \
        -keyalg RSA -keysize 2048 -validity 10000 \
        -dname "CN=Android Debug,O=Android,C=US" \
        -noprompt 2>/dev/null
fi

"$BUILD_TOOLS/apksigner" sign \
    --ks "$KEYSTORE" \
    --ks-pass pass:android \
    --ks-key-alias androiddebugkey \
    --key-pass pass:android \
    --out "$PROJECT_DIR/$APK_NAME.apk" \
    "$BUILD_DIR/$APK_NAME-aligned.apk"

echo ""
echo "=== Build Complete ==="
echo "APK: $PROJECT_DIR/$APK_NAME.apk"
ls -lh "$PROJECT_DIR/$APK_NAME.apk"
