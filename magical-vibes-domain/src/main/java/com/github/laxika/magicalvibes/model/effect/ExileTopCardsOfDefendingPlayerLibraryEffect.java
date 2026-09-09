package com.github.laxika.magicalvibes.model.effect;

/** Exiles the top {@code count} cards of the defending player's library. */
public record ExileTopCardsOfDefendingPlayerLibraryEffect(int count)
        implements CardEffect, CombatDamageTriggerContextEffect {

    @Override
    public TriggerContext combatDamageTriggerContext() {
        return TriggerContext.DAMAGED_PLAYER;
    }
}
