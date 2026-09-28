package com.github.laxika.magicalvibes.model.effect;

/** Exiles a random nonland card from the damaged player's library and tracks it with the source. */
public record ExileRandomNonlandCardFromDamagedPlayerLibraryEffect()
        implements CombatDamageTriggerContextEffect {

    @Override
    public TriggerContext combatDamageTriggerContext() {
        return TriggerContext.DAMAGED_PLAYER;
    }
}
