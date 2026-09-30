package com.github.laxika.magicalvibes.model.effect;

/** Gives the source card its printed starting intensity if it has not been initialized yet. */
public record InitializeSourceIntensityEffect(int amount) implements CardEffect {

    public InitializeSourceIntensityEffect {
        if (amount < 0) {
            throw new IllegalArgumentException("amount cannot be negative");
        }
    }
}
