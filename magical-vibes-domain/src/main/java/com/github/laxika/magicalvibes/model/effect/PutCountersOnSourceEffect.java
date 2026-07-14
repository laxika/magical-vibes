package com.github.laxika.magicalvibes.model.effect;

public record PutCountersOnSourceEffect(int powerModifier, int toughnessModifier, int amount)
        implements CombatDamageTriggerContextEffect {

    @Override
    public TriggerContext combatDamageTriggerContext() {
        return TriggerContext.SOURCE_SELF;
    }
}
