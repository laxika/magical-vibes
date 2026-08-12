package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Boosts the controller's creatures by +X/+Y for the specified duration, optionally restricted by a
 * predicate filter. The amounts are {@link DynamicAmount}s, so "gets +2/+2" and
 * "gets +X/+X where X is the greatest power among creatures you control" are the same effect
 * with different amount parameters. The amount is evaluated once on resolution and applied
 * uniformly to every matching creature.
 */
public record BoostAllOwnCreaturesEffect(
        DynamicAmount powerBoost,
        DynamicAmount toughnessBoost,
        PermanentPredicate filter,
        GrantDuration duration
) implements CardEffect {

    public BoostAllOwnCreaturesEffect(DynamicAmount powerBoost, DynamicAmount toughnessBoost) {
        this(powerBoost, toughnessBoost, null, GrantDuration.END_OF_TURN);
    }

    public BoostAllOwnCreaturesEffect(DynamicAmount powerBoost, DynamicAmount toughnessBoost,
                                      PermanentPredicate filter) {
        this(powerBoost, toughnessBoost, filter, GrantDuration.END_OF_TURN);
    }

    /** Convenience for plain fixed boosts ("all creatures you control get +1/+1"). */
    public BoostAllOwnCreaturesEffect(int powerBoost, int toughnessBoost) {
        this(new Fixed(powerBoost), new Fixed(toughnessBoost), null, GrantDuration.END_OF_TURN);
    }

    /** Convenience for plain fixed boosts restricted by a filter. */
    public BoostAllOwnCreaturesEffect(int powerBoost, int toughnessBoost, PermanentPredicate filter) {
        this(new Fixed(powerBoost), new Fixed(toughnessBoost), filter, GrantDuration.END_OF_TURN);
    }

    /** Convenience for a fixed boost with a non-default duration. */
    public BoostAllOwnCreaturesEffect(int powerBoost, int toughnessBoost, GrantDuration duration) {
        this(new Fixed(powerBoost), new Fixed(toughnessBoost), null, duration);
    }
}
