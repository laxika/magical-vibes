package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Boosts ALL creatures on the battlefield (all players) by a fixed amount until end of turn.
 * Optional predicate filter to restrict which creatures are affected.
 * Unlike {@link BoostAllOwnCreaturesEffect} which only affects the controller's creatures,
 * this affects creatures on every player's battlefield.
 */
public record BoostAllCreaturesEffect(
        int powerBoost,
        int toughnessBoost,
        PermanentPredicate filter
) implements CardEffect {

    public BoostAllCreaturesEffect(int powerBoost, int toughnessBoost) {
        this(powerBoost, toughnessBoost, null);
    }
}
