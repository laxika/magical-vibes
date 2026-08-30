package com.github.laxika.magicalvibes.model.effect;

/**
 * Marker effect: the spell is exiled instead of going to the graveyard after resolution.
 * Analogous to {@link ShuffleIntoLibraryEffect} but for exile.
 */
public record ExileSpellEffect(int screamCounterCount) implements CardEffect {

    public ExileSpellEffect() {
        this(0);
    }

    public ExileSpellEffect {
        if (screamCounterCount < 0) {
            throw new IllegalArgumentException("Scream counter count cannot be negative");
        }
    }
}
