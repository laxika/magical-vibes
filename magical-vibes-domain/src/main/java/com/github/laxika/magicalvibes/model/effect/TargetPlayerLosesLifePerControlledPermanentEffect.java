package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Target player loses X life, where X = multiplier × number of permanents
 * the controller controls matching the filter.
 * Unlike DrainLifePerControlledPermanentEffect, the controller does NOT gain life.
 * Used by Bishop of the Bloodstained and similar effects.
 */
public record TargetPlayerLosesLifePerControlledPermanentEffect(
        PermanentPredicate filter,
        int multiplier
) implements CardEffect {

    @Override
    public boolean canTargetPlayer() { return true; }
}
