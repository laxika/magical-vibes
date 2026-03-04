package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Target player loses X life and controller gains X life,
 * where X = multiplier × number of permanents the controller controls matching the filter.
 * Used by Tezzeret, Agent of Bolas -4 and similar effects.
 */
public record DrainLifePerControlledPermanentEffect(
        PermanentPredicate filter,
        int multiplier
) implements CardEffect {

    @Override
    public boolean canTargetPlayer() { return true; }
}
