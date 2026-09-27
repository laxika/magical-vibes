package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PlayerRelation;

/**
 * Targeted enter-trigger marker for an opponent losing life equal to the entering creature's
 * absolute power/toughness difference.
 */
public record TargetOpponentLosesLifeEqualToPowerToughnessDifferenceEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }

    @Override
    public PlayerRelation targetPlayerRelation() {
        return PlayerRelation.OPPONENT;
    }
}
