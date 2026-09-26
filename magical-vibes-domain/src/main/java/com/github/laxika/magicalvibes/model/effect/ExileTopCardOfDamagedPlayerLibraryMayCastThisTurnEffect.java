package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles the top card of the damaged player's library and grants the source's controller
 * permission to cast it until end of turn using mana of any type.
 */
public record ExileTopCardOfDamagedPlayerLibraryMayCastThisTurnEffect()
        implements CombatDamageTriggerContextEffect {

    @Override
    public TriggerContext combatDamageTriggerContext() {
        return TriggerContext.DAMAGED_PLAYER;
    }
}
