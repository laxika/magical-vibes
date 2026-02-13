package com.github.laxika.magicalvibes.model.effect;

public record DrawCardEffect(int amount) implements CardEffect {

    public DrawCardEffect() {
        this(1);
    }
}
