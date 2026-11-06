package com.github.laxika.magicalvibes.model.effect;

/** Exiles a random card with the combat-damage amount as its mana value from the damaged player's library. */
public record ExileRandomCardWithManaValueFromDamagedPlayerLibraryEffect()
        implements CombatDamageTriggerContextEffect {

    @Override
    public TriggerContext combatDamageTriggerContext() {
        return TriggerContext.DAMAGED_PLAYER;
    }
}
