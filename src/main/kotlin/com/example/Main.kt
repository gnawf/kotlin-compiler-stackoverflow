package com.example

import graphql.GraphqlErrorBuilder

fun main() {
    val error = GraphqlErrorBuilder.newError()
        .message("test error")
        .build()
    println("Error: $error")
}
