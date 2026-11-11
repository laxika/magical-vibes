package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.Objects;

/**
 * Puts counters on a target permanent and, when at least one counter was placed, queues a
 * reflexive triggered ability, which records what the resolving ability was about.
 */
public record PutCountersOnTargetPermanentThenReflexiveEffect(
        CounterType counterType,
        int count,
        CardEffect reflexiveEffect,
        boolean reflexiveOptionalTarget,
        PermanentPredicate targetPredicate,
        boolean reflexiveUsesTargetAsTriggeringPermanent
) implements CardEffect {

    public PutCountersOnTargetPermanentThenReflexiveEffect(CounterType counterType, int count,
                                                           CardEffect reflexiveEffect) {
        this(counterType, count, reflexiveEffect, false, null, false);
    }

    public PutCountersOnTargetPermanentThenReflexiveEffect(CounterType counterType, int count,
                                                           CardEffect reflexiveEffect,
                                                           boolean reflexiveOptionalTarget) {
        this(counterType, count, reflexiveEffect, reflexiveOptionalTarget, null, false);
    }

    public PutCountersOnTargetPermanentThenReflexiveEffect {
        Objects.requireNonNull(counterType, "counterType");
        Objects.requireNonNull(reflexiveEffect, "reflexiveEffect");
    }

    /** The initial target is the creature receiving the counters; the reflexive effect targets later. */
    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature(), targetPredicate);
    }
}
