package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles the top card of the damaged player's library and lets the controller of the creature
 * that dealt the damage play it until end of turn, using mana of any type to cast a spell.
 */
public record ExileTopCardOfDamagedPlayerLibraryAndGrantCreatureControllerPlayPermissionUntilEndOfTurnEffect()
        implements CombatDamageTriggerContextEffect {

    @Override
    public TriggerContext combatDamageTriggerContext() {
        return TriggerContext.DAMAGED_PLAYER;
    }
}
