package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/**
 * Non-targeting combat damage trigger: the damaged player sacrifices a creature of their choice.
 * If that player cannot sacrifice a creature, the source controller creates the token template.
 */
public record TargetPlayerSacrificesCreatureOrCreatesTokenEffect(CreateTokenEffect tokenTemplate)
        implements TokenCreatingEffect, CombatDamageTriggerContextEffect {

    @Override
    public DynamicAmount tokenAmount() {
        return tokenTemplate.tokenAmount();
    }

    @Override
    public CardType tokenType() {
        return tokenTemplate.tokenType();
    }

    @Override
    public int tokenPower() {
        return tokenTemplate.tokenPower();
    }

    @Override
    public int tokenToughness() {
        return tokenTemplate.tokenToughness();
    }

    @Override
    public TriggerContext combatDamageTriggerContext() {
        return TriggerContext.DAMAGED_PLAYER;
    }
}
