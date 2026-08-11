package com.github.laxika.magicalvibes.model.effect;

/**
 * Exile the top card of the damaged player's library, gain life equal to its mana value, and
 * grant the source's controller permission to cast it until end of turn using mana of any type.
 */
public record ExileTopCardOfDamagedPlayerLibraryGainLifeAndMayCastThisTurnEffect()
        implements CombatDamageTriggerContextEffect {

    @Override
    public TriggerContext combatDamageTriggerContext() {
        return TriggerContext.DAMAGED_PLAYER;
    }
}
