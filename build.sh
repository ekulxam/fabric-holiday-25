set -e

cd mod
chmod +x ./gradlew
./gradlew clean build
cd ..
cp mod/build/libs/* mods
packwiz refresh --build
mkdir -p out
packwiz modrinth export -o out/holiday-server-pack.mrpack
