package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * Removes one counter of the given type from the source permanent. If it has no such counter,
 * sacrifices the source and resolves {@code thenEffect} after the sacrifice.
 */
public record RemoveCounterOrSacrificeSelfThenEffect(CounterType counterType, CardEffect thenEffect)
        implements CombatDamageTriggerContextEffect {

    public RemoveCounterOrSacrificeSelfThenEffect {
        if (thenEffect == null) {
            throw new IllegalArgumentException("RemoveCounterOrSacrificeSelfThenEffect requires a payload");
        }
    }

    @Override
    public TargetSpec targetSpec() {
        return thenEffect.targetSpec();
    }

    @Override
    public TriggerContext combatDamageTriggerContext() {
        if (thenEffect instanceof CombatDamageTriggerContextEffect contextEffect
                && contextEffect.combatDamageTriggerContext() == TriggerContext.DAMAGED_PLAYER) {
            return TriggerContext.DAMAGED_PLAYER;
        }
        return TriggerContext.SOURCE_SELF;
    }
}
