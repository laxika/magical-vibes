package com.github.laxika.magicalvibes.model.effect;

public record DiscardCardEffect(int amount) implements CardEffect {

    public DiscardCardEffect() {
        this(1);
    }
}
