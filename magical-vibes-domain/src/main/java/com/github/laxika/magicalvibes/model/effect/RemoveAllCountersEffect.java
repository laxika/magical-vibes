package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * "Remove all {counter} counters from {this permanent | target creature}." Removes every counter of
 * exactly the given type from the {@code subject}; no-op when it carries none. The number removed is
 * snapshotted onto the stack entry as its event value, so a later effect on the same entry can
 * reference "that much" via an {@code EventValue} amount (e.g. Ashling the Pilgrim's "remove all
 * +1/+1 counters from it, and it deals that much damage to each creature and each player").
 *
 * <p>{@link CounterRemovalSubject#SOURCE} is non-targeting and is the shape that fires as an
 * {@code ON_COMBAT_DAMAGE_TO_PLAYER} trigger (Ammit Eternal), hence
 * {@link CombatDamageTriggerContextEffect.TriggerContext#SOURCE_SELF}.
 * {@link CounterRemovalSubject#TARGET} targets a creature (Hapatra's Mark) and needs no combat
 * context, so it reports {@code null}.</p>
 */
public record RemoveAllCountersEffect(CounterType counterType, CounterRemovalSubject subject)
        implements CombatDamageTriggerContextEffect {

    /** "Remove all {counter} counters from this permanent" — the non-targeting source form. */
    public RemoveAllCountersEffect(CounterType counterType) {
        this(counterType, CounterRemovalSubject.SOURCE);
    }

    @Override
    public TargetSpec targetSpec() {
        return subject == CounterRemovalSubject.TARGET
                ? TargetSpec.benign(TargetPredicates.creature())
                : new TargetSpec(null, false, null, true, 1);
    }

    @Override
    public TriggerContext combatDamageTriggerContext() {
        return subject == CounterRemovalSubject.SOURCE ? TriggerContext.SOURCE_SELF : null;
    }
}
