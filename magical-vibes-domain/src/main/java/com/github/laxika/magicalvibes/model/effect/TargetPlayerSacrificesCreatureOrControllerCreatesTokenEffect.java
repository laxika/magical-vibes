package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/**
 * The target player sacrifices a creature of their choice. If they cannot, the ability
 * controller creates the supplied token.
 */
public record TargetPlayerSacrificesCreatureOrControllerCreatesTokenEffect(CreateTokenEffect token)
        implements TokenCreatingEffect, CombatDamageTriggerContextEffect {

    @Override
    public DynamicAmount tokenAmount() {
        return token.tokenAmount();
    }

    @Override
    public CardType tokenType() {
        return token.tokenType();
    }

    @Override
    public int tokenPower() {
        return token.tokenPower();
    }

    @Override
    public int tokenToughness() {
        return token.tokenToughness();
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }

    @Override
    public TriggerContext combatDamageTriggerContext() {
        return TriggerContext.DAMAGED_PLAYER;
    }
}
