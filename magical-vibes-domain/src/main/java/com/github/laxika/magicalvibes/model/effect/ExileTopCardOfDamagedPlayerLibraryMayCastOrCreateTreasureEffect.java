package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles the top card of the combat-damaged player's library and offers it to the ability
 * controller for a resolution-time cast. If the card is not cast, the controller creates a
 * Treasure token.
 */
public record ExileTopCardOfDamagedPlayerLibraryMayCastOrCreateTreasureEffect()
        implements CombatDamageTriggerContextEffect {

    @Override
    public TriggerContext combatDamageTriggerContext() {
        return TriggerContext.DAMAGED_PLAYER;
    }
}
