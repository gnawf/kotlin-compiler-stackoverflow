package com.example

fun main() {
    val result = ConcreteBuilder.newBuilder()
        .name("test")
        .build()
    println("Result: $result")
}
