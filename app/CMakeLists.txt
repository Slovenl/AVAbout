
cmake_minimum_required(VERSION 3.4.1)


add_library(
             native-lib
             SHARED
             src/main/cpp/native-lib.cpp )


find_library(
              log-lib
              log )

set(audiorecord_DIR ${CMAKE_SOURCE_DIR}/../AudioRecorderLib)
include_directories(${audiorecord_DIR}/src/main/cpp/include)
add_library(audiorecord SHARED IMPORTED)
set_target_properties(audiorecord PROPERTIES IMPORTED_LOCATION
                      ${audiorecord_DIR}/libs/${ANDROID_ABI}/libaudiorecord.so)


target_link_libraries(
                       native-lib
                       audiorecord
                       ${log-lib}
                     )