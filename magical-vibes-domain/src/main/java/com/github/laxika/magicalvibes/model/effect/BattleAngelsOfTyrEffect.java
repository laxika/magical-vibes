package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Resolves Battle Angels of Tyr's combat-damage riders against the damaged player.
 */
public record BattleAngelsOfTyrEffect() implements CardDrawingEffect, TokenCreatingEffect,
        LifeGainEffect, CombatDamageTriggerContextEffect {

    @Override
    public DynamicAmount drawnCardAmount() {
        return new Fixed(1);
    }

    @Override
    public DynamicAmount tokenAmount() {
        return new Fixed(1);
    }

    @Override
    public CardType tokenType() {
        return CardType.ARTIFACT;
    }

    @Override
    public int tokenPower() {
        return 0;
    }

    @Override
    public int tokenToughness() {
        return 0;
    }

    @Override
    public DynamicAmount lifeGainAmount() {
        return new Fixed(3);
    }

    @Override
    public TriggerContext combatDamageTriggerContext() {
        return TriggerContext.DAMAGED_PLAYER;
    }
}
