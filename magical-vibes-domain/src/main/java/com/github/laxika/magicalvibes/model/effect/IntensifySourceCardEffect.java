package com.github.laxika.magicalvibes.model.effect;

/** Increases the persistent intensity of the physical card that supplies the effect. */
public record IntensifySourceCardEffect(int amount) implements CardEffect {

    public IntensifySourceCardEffect {
        if (amount <= 0) {
            throw new IllegalArgumentException("Intensity amount must be positive");
        }
    }
}
