package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * "Can't have counters put on it" (Melira's Keepers, Tatterkite; "enchanted creature can't have counters put on it",
 * Blossombind) or, with a {@code counterType}, "can't have [kind] counters put on it": "creatures you control can't
 * have -1/-1 counters put on them" (Melira, Sylvok Outcast, granted by {@code GrantEffectEffect}), "creatures your
 * opponents control can't have +1/+1 counters put on them" (Blightbeetle, over a {@code controlledBy} subject).
 *
 * <p>{@code subject} names what the restriction is on: the source, the permanent the source is attached to, or (for a
 * kind) a set of creatures the source's static ability names, read as a rule of that source so the recipient losing its
 * abilities doesn't lift it. A {@code null} {@code counterType} is every kind; otherwise only
 * {@link CounterType#PLUS_ONE_PLUS_ONE} and {@link CounterType#MINUS_ONE_MINUS_ONE} are read
 * ({@code GameQueryService.cantHaveCounters}, {@code cantHavePlusOnePlusOneCounters},
 * {@code cantHaveMinusOneMinusOneCounters}).</p>
 */
public record CantHaveCountersEffect(PermanentSubject subject, CounterType counterType) implements CardEffect {

    public CantHaveCountersEffect {
        if (subject == null) {
            subject = Subjects.source();
        }
        if (counterType != null && counterType != CounterType.PLUS_ONE_PLUS_ONE
                && counterType != CounterType.MINUS_ONE_MINUS_ONE) {
            throw new IllegalArgumentException("Only +1/+1 and -1/-1 counter restrictions are read: " + counterType);
        }
    }

    /** "This permanent can't have counters put on it." */
    public CantHaveCountersEffect() {
        this(Subjects.source(), null);
    }

    /** No counters of any kind on the permanent {@code subject} names. */
    public CantHaveCountersEffect(PermanentSubject subject) {
        this(subject, null);
    }

    /** "Can't have [kind] counters put on it": no {@code counterType} counters on the permanent it is on. */
    public static CantHaveCountersEffect ofKind(CounterType counterType) {
        return new CantHaveCountersEffect(Subjects.source(), counterType);
    }
}
