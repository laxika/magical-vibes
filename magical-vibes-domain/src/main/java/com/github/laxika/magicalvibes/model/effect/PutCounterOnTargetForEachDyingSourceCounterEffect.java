package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Death trigger for "When this creature dies, put a {@code counterType} counter on target creature
 * for each {@code counterType} counter on this creature" (e.g. Grief Tyrant, which enters with four
 * -1/-1 counters and hands them out on death).
 * <p>
 * The counters are counted on the stack entry's triggering permanent, as it last existed on the battlefield
 * (CR 603.10a): the source is off the battlefield by the time this resolves.
 *
 * @param counterType the counter type counted on the dying creature and placed on the target
 * @param optional    when {@code true} this is a "you may" death trigger (Soulstinger) — the target
 *                    is still chosen as the trigger goes on the stack (CR 603.3d), but the controller
 *                    may decline placing the counters at resolution; the collector wraps the effect
 *                    in a {@code MayEffect}
 * @param targetPredicate optional structural restriction for the target; {@code null} means any creature
 * @param modular whether the counter placement is the result of a modular triggered ability
 */
public record PutCounterOnTargetForEachDyingSourceCounterEffect(
        CounterType counterType,
        boolean optional,
        PermanentPredicate targetPredicate,
        boolean modular
) implements CardEffect {

    public PutCounterOnTargetForEachDyingSourceCounterEffect(CounterType counterType) {
        this(counterType, false, null, false);
    }

    public PutCounterOnTargetForEachDyingSourceCounterEffect(CounterType counterType, boolean optional) {
        this(counterType, optional, null, false);
    }

    public PutCounterOnTargetForEachDyingSourceCounterEffect(
            CounterType counterType,
            boolean optional,
            PermanentPredicate targetPredicate
    ) {
        this(counterType, optional, targetPredicate, false);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.permanent(),
                targetPredicate != null ? targetPredicate : new PermanentIsCreaturePredicate());
    }
}
