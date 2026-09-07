package com.github.laxika.magicalvibes.model.effect;

/**
 * Marker effect: the spell is exiled instead of going to the graveyard after resolution.
 * Analogous to {@link ShuffleIntoLibraryEffect} but for exile.
 *
 * @param suspendTimeCounters number of suspend time counters to put on the exiled spell
 * @param screamCounterCount number of scream counters to put on the exiled spell
 */
public record ExileSpellEffect(int suspendTimeCounters, int screamCounterCount) implements CardEffect {

    public ExileSpellEffect() {
        this(0, 0);
    }

    public ExileSpellEffect(int suspendTimeCounters) {
        this(suspendTimeCounters, 0);
    }

    public static ExileSpellEffect withScreamCounters(int screamCounterCount) {
        return new ExileSpellEffect(0, screamCounterCount);
    }

    public ExileSpellEffect {
        if (suspendTimeCounters < 0) {
            throw new IllegalArgumentException("suspendTimeCounters cannot be negative");
        }
        if (screamCounterCount < 0) {
            throw new IllegalArgumentException("Scream counter count cannot be negative");
        }
    }
}
