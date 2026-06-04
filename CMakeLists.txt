cmake_minimum_required(VERSION 3.4.1)

project(terminal-lib)

add_library(neoterm
             SHARED
             src/main/cpp/neoterm.cpp)

target_link_libraries(neoterm)
