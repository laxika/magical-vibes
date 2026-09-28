package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/** Exiles a random creature card from the damaged player's library and copies it as a token. */
public record ExileRandomCreatureCardFromDamagedPlayerLibraryAndCreateTokenCopyEffect(
        CreateTokenCopyOfTargetPermanentEffect tokenCopyEffect
) implements TokenCreatingEffect, CombatDamageTriggerContextEffect {

    @Override
    public DynamicAmount tokenAmount() {
        return new Fixed(1);
    }

    @Override
    public CardType tokenType() {
        return CardType.CREATURE;
    }

    @Override
    public int tokenPower() {
        return 1;
    }

    @Override
    public int tokenToughness() {
        return 1;
    }

    @Override
    public TriggerContext combatDamageTriggerContext() {
        return TriggerContext.DAMAGED_PLAYER;
    }
}
