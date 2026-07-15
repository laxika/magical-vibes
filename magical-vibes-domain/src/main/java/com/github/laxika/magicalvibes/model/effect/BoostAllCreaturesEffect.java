package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Boosts creatures by +X/+Y until end of turn, optionally restricted by a predicate filter.
 * The {@link EachPermanentScope} selects whose battlefield is scanned: {@link EachPermanentScope#ALL_PLAYERS}
 * affects every player's creatures (unlike {@link BoostAllOwnCreaturesEffect} which only affects the
 * controller's), while {@link EachPermanentScope#TARGET_PLAYER} affects only the creatures the targeted
 * player controls ("creatures target player controls get +X/+Y", e.g. Shields of Velis Vel). The amounts
 * are {@link DynamicAmount}s (so "all creatures get +X/-X where X was paid" is this effect with
 * {@code XValue}-based amounts), evaluated once on resolution and applied uniformly.
 */
public record BoostAllCreaturesEffect(
        DynamicAmount powerBoost,
        DynamicAmount toughnessBoost,
        PermanentPredicate filter,
        EachPermanentScope scope
) implements CardEffect {

    public BoostAllCreaturesEffect(DynamicAmount powerBoost, DynamicAmount toughnessBoost, PermanentPredicate filter) {
        this(powerBoost, toughnessBoost, filter, EachPermanentScope.ALL_PLAYERS);
    }

    public BoostAllCreaturesEffect(DynamicAmount powerBoost, DynamicAmount toughnessBoost) {
        this(powerBoost, toughnessBoost, null, EachPermanentScope.ALL_PLAYERS);
    }

    /** Convenience for plain fixed boosts ("all creatures get +1/+1"). */
    public BoostAllCreaturesEffect(int powerBoost, int toughnessBoost) {
        this(new Fixed(powerBoost), new Fixed(toughnessBoost), null, EachPermanentScope.ALL_PLAYERS);
    }

    /** Convenience for plain fixed boosts restricted by a filter. */
    public BoostAllCreaturesEffect(int powerBoost, int toughnessBoost, PermanentPredicate filter) {
        this(new Fixed(powerBoost), new Fixed(toughnessBoost), filter, EachPermanentScope.ALL_PLAYERS);
    }

    /** Convenience for "creatures target player controls get +X/+Y". */
    public BoostAllCreaturesEffect(int powerBoost, int toughnessBoost, EachPermanentScope scope) {
        this(new Fixed(powerBoost), new Fixed(toughnessBoost), null, scope);
    }

    @Override
    public TargetSpec targetSpec() {
        return scope == EachPermanentScope.TARGET_PLAYER
                ? TargetSpec.benign(TargetCategory.PLAYER) : TargetSpec.NONE;
    }
}
