package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Boosts ALL creatures on the battlefield (every player) by +X/+Y until end of turn, optionally
 * restricted by a predicate filter. Unlike {@link BoostAllOwnCreaturesEffect} which only affects
 * the controller's creatures, this affects creatures on every player's battlefield. The amounts
 * are {@link DynamicAmount}s (so "all creatures get +X/-X where X was paid" is this effect with
 * {@code XValue}-based amounts), evaluated once on resolution and applied uniformly.
 */
public record BoostAllCreaturesEffect(
        DynamicAmount powerBoost,
        DynamicAmount toughnessBoost,
        PermanentPredicate filter
) implements CardEffect {

    public BoostAllCreaturesEffect(DynamicAmount powerBoost, DynamicAmount toughnessBoost) {
        this(powerBoost, toughnessBoost, null);
    }

    /** Convenience for plain fixed boosts ("all creatures get +1/+1"). */
    public BoostAllCreaturesEffect(int powerBoost, int toughnessBoost) {
        this(new Fixed(powerBoost), new Fixed(toughnessBoost), null);
    }

    /** Convenience for plain fixed boosts restricted by a filter. */
    public BoostAllCreaturesEffect(int powerBoost, int toughnessBoost, PermanentPredicate filter) {
        this(new Fixed(powerBoost), new Fixed(toughnessBoost), filter);
    }
}
