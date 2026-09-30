package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PlayerRelation;

/** Removes all counters from a target permanent or opponent and records the amount removed. */
public record RemoveAllCountersFromTargetPermanentOrPlayerEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.playerOrPermanent());
    }

    @Override
    public PlayerRelation targetPlayerRelation() {
        return PlayerRelation.OPPONENT;
    }

    @Override
    public boolean hasOptionalTarget() {
        return true;
    }
}
