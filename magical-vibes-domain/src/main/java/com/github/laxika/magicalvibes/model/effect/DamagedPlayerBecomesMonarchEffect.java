package com.github.laxika.magicalvibes.model.effect;

/** Makes the player dealt combat damage by this source become the monarch. */
public record DamagedPlayerBecomesMonarchEffect() implements CombatDamageTriggerContextEffect {

    @Override
    public TriggerContext combatDamageTriggerContext() {
        return TriggerContext.DAMAGED_PLAYER;
    }
}
