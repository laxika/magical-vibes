package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Boosts the controller's creatures by +X/+Y until end of turn, optionally restricted by a
 * predicate filter. The amounts are {@link DynamicAmount}s, so "gets +2/+2" and
 * "gets +X/+X where X is the greatest power among creatures you control" are the same effect
 * with different amount parameters. The amount is evaluated once on resolution and applied
 * uniformly to every matching creature.
 */
public record BoostAllOwnCreaturesEffect(
        DynamicAmount powerBoost,
        DynamicAmount toughnessBoost,
        PermanentPredicate filter
) implements CardEffect {

    public BoostAllOwnCreaturesEffect(DynamicAmount powerBoost, DynamicAmount toughnessBoost) {
        this(powerBoost, toughnessBoost, null);
    }

    /** Convenience for plain fixed boosts ("all creatures you control get +1/+1"). */
    public BoostAllOwnCreaturesEffect(int powerBoost, int toughnessBoost) {
        this(new Fixed(powerBoost), new Fixed(toughnessBoost), null);
    }

    /** Convenience for plain fixed boosts restricted by a filter. */
    public BoostAllOwnCreaturesEffect(int powerBoost, int toughnessBoost, PermanentPredicate filter) {
        this(new Fixed(powerBoost), new Fixed(toughnessBoost), filter);
    }
}
