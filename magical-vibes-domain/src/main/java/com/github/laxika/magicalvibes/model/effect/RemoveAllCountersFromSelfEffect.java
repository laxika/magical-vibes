package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * Remove all counters of the given type from this permanent. The number of counters removed is
 * snapshotted onto the stack entry as its event value, so a later effect on the same entry can
 * reference "that much" via an {@code EventValue} amount (e.g. Ashling the Pilgrim's "remove all
 * +1/+1 counters from it, and it deals that much damage to each creature and each player").
 */
public record RemoveAllCountersFromSelfEffect(CounterType counterType)
        implements CombatDamageTriggerContextEffect {

    @Override
    public TargetSpec targetSpec() {
        return new TargetSpec(TargetCategory.NONE, false, null, true, 1);
    }

    @Override
    public TriggerContext combatDamageTriggerContext() {
        return TriggerContext.SOURCE_SELF;
    }
}
