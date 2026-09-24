package com.github.laxika.magicalvibes.model.effect;

/** Adds percentage points to the controller's persistent coolness total. */
public record CoolnessEffect(int amount) implements CardEffect {

    public CoolnessEffect {
        if (amount <= 0) {
            throw new IllegalArgumentException("Coolness change must be positive");
        }
    }
}
