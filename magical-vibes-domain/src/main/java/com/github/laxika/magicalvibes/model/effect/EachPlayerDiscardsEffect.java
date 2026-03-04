package com.github.laxika.magicalvibes.model.effect;

public record EachPlayerDiscardsEffect(int amount) implements CardEffect {

    public EachPlayerDiscardsEffect() {
        this(1);
    }
}
