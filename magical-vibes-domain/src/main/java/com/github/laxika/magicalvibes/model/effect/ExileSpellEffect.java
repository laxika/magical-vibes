package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Marker effect: the spell is exiled instead of going to the graveyard after resolution.
 * Analogous to {@link ShuffleIntoLibraryEffect} but for exile.
 *
 * @param suspendTimeCounters number of suspend time counters to put on the exiled spell
 * @param screamCounterCount number of scream counters to put on the exiled spell
 * @param sourcePermanentId the permanent that should track the exiled spell, if any
 */
public record ExileSpellEffect(int suspendTimeCounters, int screamCounterCount,
                               UUID sourcePermanentId) implements CardEffect {

    public ExileSpellEffect() {
        this(0, 0, null);
    }

    public ExileSpellEffect(int suspendTimeCounters) {
        this(suspendTimeCounters, 0, null);
    }

    public ExileSpellEffect(int suspendTimeCounters, int screamCounterCount) {
        this(suspendTimeCounters, screamCounterCount, null);
    }

    public static ExileSpellEffect withScreamCounters(int screamCounterCount) {
        return new ExileSpellEffect(0, screamCounterCount, null);
    }

    public static ExileSpellEffect withSource(UUID sourcePermanentId) {
        return new ExileSpellEffect(0, 0, sourcePermanentId);
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
