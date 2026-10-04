package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/**
 * Exiles the top card of the combat-damaged player's library. If it is a permanent card, the
 * effect's controller may put it onto the battlefield under their control; otherwise, or if they
 * decline, the fallback token is created.
 */
public record ExileTopCardOfDamagedPlayerLibraryMayPutPermanentOntoBattlefieldOrCreateTokenEffect(
        CreateTokenEffect fallbackToken
) implements CombatDamageTriggerContextEffect, TokenCreatingEffect {

    @Override
    public TriggerContext combatDamageTriggerContext() {
        return TriggerContext.DAMAGED_PLAYER;
    }

    @Override
    public DynamicAmount tokenAmount() {
        return fallbackToken.tokenAmount();
    }

    @Override
    public CardType tokenType() {
        return fallbackToken.tokenType();
    }

    @Override
    public int tokenPower() {
        return fallbackToken.tokenPower();
    }

    @Override
    public int tokenToughness() {
        return fallbackToken.tokenToughness();
    }
}
