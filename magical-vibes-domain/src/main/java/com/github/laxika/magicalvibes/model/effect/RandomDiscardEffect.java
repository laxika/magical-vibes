package com.github.laxika.magicalvibes.model.effect;

public record RandomDiscardEffect(int amount) implements CardEffect {

    public RandomDiscardEffect() {
        this(1);
    }
}
