package com.github.laxika.magicalvibes.model.effect;

/**
 * ON_ANY_OTHER_CREATURE_ENTERS_BATTLEFIELD value-materialising effect: put a number of
 * {@code powerModifier}/{@code toughnessModifier} counters on this creature equal to the
 * entering creature's power. The entering creature's power is read at trigger time and baked
 * into a concrete {@link PutCountersOnSourceEffect} (mirroring {@link GainLifeEqualToToughnessEffect}).
 * When {@code optional} is true the ability is queued as a "you may" so the choice happens at
 * resolution. Used by Hamletback Goliath ({@code (1, 1, true)}).
 */
public record PutCountersOnSourceEqualToEnteringPowerEffect(int powerModifier, int toughnessModifier, boolean optional)
        implements CardEffect {
}
