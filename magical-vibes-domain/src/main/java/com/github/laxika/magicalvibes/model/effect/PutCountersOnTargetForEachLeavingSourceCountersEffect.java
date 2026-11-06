package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Leaves-the-battlefield trigger that puts the same number of each counter type on a target
 * permanent as the permanent that left had: the stack entry's triggering permanent, as it last existed on the
 * battlefield (CR 603.10a).
 *
 * @param targetPredicate optional additional predicate for the target permanent
 */
public record PutCountersOnTargetForEachLeavingSourceCountersEffect(PermanentPredicate targetPredicate)
        implements CardEffect {

    public PutCountersOnTargetForEachLeavingSourceCountersEffect() {
        this(null);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.permanent(),
                targetPredicate != null ? targetPredicate : new PermanentIsCreaturePredicate());
    }
}
