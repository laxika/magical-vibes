package com.github.laxika.magicalvibes.model.effect;

/**
 * ON_ANY_CREATURE_DIES value-materialising effect: put a number of
 * {@code powerModifier}/{@code toughnessModifier} counters on this creature equal to the dying
 * creature's last-known effective power. The dying creature's power is read at trigger time and
 * baked into a concrete {@link PutCountersOnSourceEffect} (the death sibling of
 * {@link PutCountersOnSourceEqualToEnteringPowerEffect}). When {@code optional} is true the ability
 * is queued as a "you may" so the choice happens at resolution. Used by Kresh the Bloodbraided
 * ({@code (1, 1, true)}).
 */
public record PutCountersOnSourceEqualToDyingPowerEffect(int powerModifier, int toughnessModifier, boolean optional)
        implements CardEffect {
}
