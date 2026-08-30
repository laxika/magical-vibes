package com.github.laxika.magicalvibes.model.effect;

/**
 * Marker effect: the spell is exiled instead of going to the graveyard after resolution.
 * Analogous to {@link ShuffleIntoLibraryEffect} but for exile.
 *
 * @param suspendTimeCounters number of time counters to put on the exiled spell, or zero when
 *                            the spell is simply exiled
 */
public record ExileSpellEffect(int suspendTimeCounters) implements CardEffect {

    public ExileSpellEffect() {
        this(0);
    }

    public ExileSpellEffect {
        if (suspendTimeCounters < 0) {
            throw new IllegalArgumentException("suspendTimeCounters cannot be negative");
        }
    }
}
