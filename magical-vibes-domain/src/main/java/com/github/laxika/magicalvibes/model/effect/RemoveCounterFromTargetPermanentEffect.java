package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * "Remove {up to N} counters from target permanent."
 *
 * <p>When {@code counterType} is {@code null}, removes counters of any one kind currently on the
 * target (if several kinds are present, the first present kind). No-op when the target has no
 * counters. Used by Medicine Runner.</p>
 *
 * <p>When {@code counterType} is set, removes counters of exactly that type ("remove a -1/-1 counter
 * from target creature", "remove up to four charge counters from target noncreature artifact"); no-op
 * when the target carries none of that type. Used by Defiant Greatmaw
 * ({@code MINUS_ONE_MINUS_ONE}) and Gremlin Mine ({@code CHARGE}, 4).</p>
 *
 * <p>{@code amount} is an upper bound, not a requirement: fewer counters than that are removed when
 * the target carries fewer. {@code targetPredicate} narrows which permanents are legal targets
 * ({@code null} = any permanent); e.g. Defiant Greatmaw restricts to "another creature you
 * control".</p>
 */
public record RemoveCounterFromTargetPermanentEffect(CounterType counterType,
                                                     PermanentPredicate targetPredicate,
                                                     int amount) implements CardEffect {

    /** "Remove a counter from target permanent" — any kind, any permanent (Medicine Runner). */
    public RemoveCounterFromTargetPermanentEffect() {
        this(null, null, 1);
    }

    /** "Remove a {counterType} counter from target permanent" — a single counter of one type. */
    public RemoveCounterFromTargetPermanentEffect(CounterType counterType, PermanentPredicate targetPredicate) {
        this(counterType, targetPredicate, 1);
    }

    @Override
    public TargetSpec targetSpec() {
        return targetPredicate != null
                ? TargetSpec.benign(TargetPredicates.creature(), targetPredicate)
                : TargetSpec.benign(TargetPredicates.permanent());
    }
}
