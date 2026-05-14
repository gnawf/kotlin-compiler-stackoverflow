package com.example;

import org.jspecify.annotations.NullUnmarked;

public interface Builder<B extends Builder<B>> {
    B name(String name);
    String build();
}
