# Kotlin K2 Compiler StackOverflowError — Minimal Reproducer

## Summary

The Kotlin K2 compiler (FIR) crashes with a `StackOverflowError` during compilation when
all of the following conditions are met:

1. A Java package is annotated with `@NullMarked` (JSpecify)
2. A Java interface with a recursive type bound is defined in that package (e.g. `Builder<B extends Builder<B>>`)
3. A class in the same package implements that interface and is annotated `@NullUnmarked`
4. Kotlin code calls a chained method on that class

The infinite recursion occurs in `AbstractConeSubstitutor` as the FIR type substitutor
fails to terminate when resolving the recursive type bound under mixed JSpecify nullability.

## Environment

| | |
|---|---|
| Kotlin | 2.3.21 |
| JDK | 21 (Amazon Corretto) |
| JSpecify | 1.0.0 |

## Key Files

### `package-info.java` — marks entire package as null-safe
```java
@NullMarked
package com.example;

import org.jspecify.annotations.NullMarked;
```

### `Builder.java` — interface with a recursive type bound
```java
public interface Builder<B extends Builder<B>> {
    B name(String name);
    String build();
}
```

### `ConcreteBuilder.java` — `@NullUnmarked` implementation of the recursive interface
```java
@NullUnmarked
public class ConcreteBuilder<B extends ConcreteBuilder<B>> implements Builder<B> {
    // ...
    public static ConcreteBuilder<?> newBuilder() { ... }
}
```

### `Main.kt` — Kotlin code that calls the builder chain
```kotlin
fun main() {
    val result = ConcreteBuilder.newBuilder()
        .name("test")
        .build()
    println("Result: $result")
}
```

## How to Reproduce

```bash
./gradlew compileKotlin
```

## Expected Result

Compilation succeeds and the program prints `Result: <toString>`.

## Actual Result

The Kotlin compiler crashes with:

```
> Task :compileKotlin FAILED
e: java.lang.StackOverflowError
    at org.jetbrains.kotlin.fir.types.ConeCapturedType.copy$default(ConeTypes.kt)
    at org.jetbrains.kotlin.fir.types.TypeUtilsKt.withNullability(TypeUtils.kt:287)
    at org.jetbrains.kotlin.fir.resolve.substitution.AbstractConeSubstitutor.substituteRecursive(AbstractConeSubstitutor.kt:189)
    at org.jetbrains.kotlin.fir.resolve.substitution.AbstractConeSubstitutor.substituteOrNull(AbstractConeSubstitutor.kt:42)
    at org.jetbrains.kotlin.fir.resolve.substitution.AbstractConeSubstitutor.substituteArgument(AbstractConeSubstitutor.kt:18)
    at org.jetbrains.kotlin.fir.resolve.substitution.AbstractConeSubstitutor.substituteArguments(AbstractConeSubstitutor.kt:112)
    at org.jetbrains.kotlin.fir.resolve.substitution.AbstractConeSubstitutor.substituteRecursive(AbstractConeSubstitutor.kt:70)
    at org.jetbrains.kotlin.fir.resolve.substitution.AbstractConeSubstitutor.substituteOrNull(AbstractConeSubstitutor.kt:42)
    ... (repeating indefinitely)
```

## Root Cause

The K2 FIR type substitutor enters infinite recursion when performing type substitution
on a recursive type bound (`B extends Builder<B>`) that is annotated with JSpecify
`@NullMarked` at the package level but `@NullUnmarked` on the implementing class.
The mixed nullability causes the substitutor to loop indefinitely between
`substituteRecursive` and `substituteOrNull`.

This issue was originally discovered via `com.graphql-java:graphql-java:0.0.0-2026-02-11T23-30-56-2f0dc5b`,
which uses the same pattern (`GraphqlErrorBuilder<B extends GraphqlErrorBuilder<B>>` — [permalink](https://github.com/graphql-java/graphql-java/blob/bd529fb8a80b26ced3a973ccbf5707b01f5bd8f4/src/main/java/graphql/GraphqlErrorBuilder.java#L26)) with
`@NullMarked` on the package and `@NullUnmarked` on the class).
