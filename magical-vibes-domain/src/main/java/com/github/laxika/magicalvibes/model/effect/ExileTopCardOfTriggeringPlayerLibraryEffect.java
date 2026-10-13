package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles the top card of the player identified by the resolving trigger entry. As an
 * {@code ON_COMBAT_DAMAGE_TO_PLAYER} trigger the entry binds the damaged player (ingest).
 */
public record ExileTopCardOfTriggeringPlayerLibraryEffect() implements CombatDamageTriggerContextEffect {

    @Override
    public TriggerContext combatDamageTriggerContext() {
        return TriggerContext.DAMAGED_PLAYER;
    }
}
