package com.github.laxika.magicalvibes.model.effect;

/** Goads each creature controlled by the player dealt combat damage until the controller's next turn. */
public record GoadCreaturesDamagedPlayerControlsUntilNextTurnEffect()
        implements CombatDamageTriggerContextEffect {

    @Override
    public TriggerContext combatDamageTriggerContext() {
        return TriggerContext.DAMAGED_PLAYER;
    }
}
