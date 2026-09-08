package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Leaves-the-battlefield trigger that puts one named counter on the target creature for each
 * counter of that type on the leaving source.
 *
 * <p>The trigger collector snapshots the source counter count before target selection because the
 * source is no longer on the battlefield when the ability resolves.
 *
 * @param counterType the counter type counted on the leaving source and placed on the target
 * @param count snapshot of the leaving source's counter count
 * @param targetPredicate optional structural restriction for the target; {@code null} means any creature
 */
public record PutCounterOnTargetForEachLeavingSourceCounterEffect(
        CounterType counterType,
        int count,
        PermanentPredicate targetPredicate
) implements CardEffect {

    public PutCounterOnTargetForEachLeavingSourceCounterEffect(CounterType counterType) {
        this(counterType, 0, null);
    }

    public PutCounterOnTargetForEachLeavingSourceCounterEffect(
            CounterType counterType,
            PermanentPredicate targetPredicate
    ) {
        this(counterType, 0, targetPredicate);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.permanent(),
                targetPredicate != null ? targetPredicate : new PermanentIsCreaturePredicate());
    }
}
