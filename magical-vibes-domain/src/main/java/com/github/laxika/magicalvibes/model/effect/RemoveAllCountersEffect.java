package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * "Remove all {counter} counters from {this permanent | target creature}." Removes every counter of
 * exactly the given type from the {@code subject}; no-op when it carries none. The number removed is
 * snapshotted onto the stack entry as its event value, so a later effect on the same entry can
 * reference "that much" via an {@code EventValue} amount (e.g. Ashling the Pilgrim's "remove all
 * +1/+1 counters from it, and it deals that much damage to each creature and each player").
 *
 * <p>{@link Subjects#source()} is non-targeting and is the shape that fires as an
 * {@code Triggers.dealsCombatDamageToPlayer()} ability (Ammit Eternal), acting on the entry's source.
 * {@link Subjects#target()} targets a creature (Hapatra's Mark). {@link Subjects#named(UUID)} acts on a permanent
 * a resolving effect bound without targeting it (Giant Oyster's delayed ability, through {@link #aimedAt}).</p>
 */
public record RemoveAllCountersEffect(CounterType counterType, PermanentSubject subject) implements CardEffect {

    public RemoveAllCountersEffect {
        if (subject == null) {
            subject = Subjects.source();
        }
    }

    /** "Remove all {counter} counters from this permanent" — the non-targeting source form. */
    public RemoveAllCountersEffect(CounterType counterType) {
        this(counterType, Subjects.source());
    }

    /** This effect on the permanent {@code namedPermanentId}, which it doesn't target. */
    public RemoveAllCountersEffect aimedAt(UUID namedPermanentId) {
        return new RemoveAllCountersEffect(counterType, Subjects.named(namedPermanentId));
    }

    @Override
    public TargetSpec targetSpec() {
        if (subject.declaresTarget()) {
            return TargetSpec.benign(TargetPredicates.creature());
        }
        return subject instanceof PermanentSubject.Source
                ? new TargetSpec(null, false, null, true, 1)
                : TargetSpec.NONE;
    }
}
