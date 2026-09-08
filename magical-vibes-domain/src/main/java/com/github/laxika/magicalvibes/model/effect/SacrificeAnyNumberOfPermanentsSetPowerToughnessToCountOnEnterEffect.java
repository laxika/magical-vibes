package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * As this permanent enters, sacrifice any number of matching permanents. Its power and toughness
 * are each set to the number sacrificed as it enters.
 */
public record SacrificeAnyNumberOfPermanentsSetPowerToughnessToCountOnEnterEffect(
        PermanentPredicate filter) implements ReplacementEffect {
}
