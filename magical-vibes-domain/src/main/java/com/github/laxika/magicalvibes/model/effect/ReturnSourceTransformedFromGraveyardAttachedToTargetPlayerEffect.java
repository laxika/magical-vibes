package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PlayerRelation;

/**
 * Returns the source card from its graveyard to the battlefield transformed under its
 * controller's control and attaches the resulting permanent to the targeted player.
 */
public record ReturnSourceTransformedFromGraveyardAttachedToTargetPlayerEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }

    @Override
    public PlayerRelation targetPlayerRelation() {
        return PlayerRelation.OPPONENT;
    }
}
