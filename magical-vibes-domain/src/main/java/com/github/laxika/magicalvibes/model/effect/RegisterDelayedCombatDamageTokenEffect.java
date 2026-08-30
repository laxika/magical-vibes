package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PlayerRelation;

/** Registers a rest-of-turn trigger for creatures controlled by the spell's controller. */
public record RegisterDelayedCombatDamageTokenEffect(CreateTokenEffect tokenEffect) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }

    @Override
    public PlayerRelation targetPlayerRelation() {
        return PlayerRelation.OPPONENT;
    }
}
