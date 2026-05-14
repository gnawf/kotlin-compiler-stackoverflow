package com.example;

import org.jspecify.annotations.NullUnmarked;

@NullUnmarked
public class ConcreteBuilder<B extends ConcreteBuilder<B>> implements Builder<B> {
    private String name;

    @Override
    public B name(String name) {
        this.name = name;
        return (B) this;
    }

    @Override
    public String build() {
        return name;
    }

    public static ConcreteBuilder<?> newBuilder() {
        return new ConcreteBuilder<>();
    }
}
