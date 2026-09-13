package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PlayerRelation;

/**
 * Puts a creature card chosen at random from the target opponent's graveyard onto the battlefield
 * under the effect controller's control.
 */
public record PutRandomCreatureFromTargetOpponentGraveyardOntoBattlefieldEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }

    @Override
    public PlayerRelation targetPlayerRelation() {
        return PlayerRelation.OPPONENT;
    }
}
