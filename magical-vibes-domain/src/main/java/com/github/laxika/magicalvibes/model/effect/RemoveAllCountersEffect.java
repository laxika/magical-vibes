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
 * <p>{@link CounterRemovalSubject#SOURCE} is non-targeting and is the shape that fires as an
 * {@code Triggers.dealsCombatDamageToPlayer()} ability (Ammit Eternal), acting on the entry's source.
 * {@link CounterRemovalSubject#TARGET} targets a creature (Hapatra's Mark). {@link CounterRemovalSubject#NAMED}
 * acts on {@code permanentId} without targeting it (Giant Oyster's delayed ability).</p>
 */
public record RemoveAllCountersEffect(CounterType counterType, CounterRemovalSubject subject, UUID permanentId)
        implements CardEffect {

    /** "Remove all {counter} counters from {this permanent | target creature}". */
    public RemoveAllCountersEffect(CounterType counterType, CounterRemovalSubject subject) {
        this(counterType, subject, null);
    }

    /** This effect on the permanent {@code namedPermanentId}, which it doesn't target. */
    public RemoveAllCountersEffect aimedAt(UUID namedPermanentId) {
        return new RemoveAllCountersEffect(counterType, CounterRemovalSubject.NAMED, namedPermanentId);
    }

    /** "Remove all {counter} counters from this permanent" — the non-targeting source form. */
    public RemoveAllCountersEffect(CounterType counterType) {
        this(counterType, CounterRemovalSubject.SOURCE, null);
    }

    @Override
    public TargetSpec targetSpec() {
        return switch (subject) {
            case TARGET -> TargetSpec.benign(TargetPredicates.creature());
            case NAMED -> TargetSpec.NONE;
            case SOURCE -> new TargetSpec(null, false, null, true, 1);
        };
    }
}
