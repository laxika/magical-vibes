package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Returns each chosen target permanent to its owner's hand, then resolves an existing rider once
 * with a derived event value. With {@link EventStat#NONE}, the value is the number of permanents
 * actually returned; otherwise it is the sum of the selected last-known statistic across returned
 * permanents.
 *
 * @param targetFilter optional permanent predicate narrowing the targets
 * @param resolveThenEffectIfNoPermanentReturned whether to resolve the rider even when no target
 *                                               permanent was returned
 */
public record ReturnTargetPermanentsThenEffect(EventStat stat, CardEffect thenEffect,
                                               PermanentPredicate targetFilter,
                                               boolean resolveThenEffectIfNoPermanentReturned)
        implements RemovalEffect {

    public ReturnTargetPermanentsThenEffect(CardEffect thenEffect) {
        this(EventStat.NONE, thenEffect, null, false);
    }

    public ReturnTargetPermanentsThenEffect(EventStat stat, CardEffect thenEffect) {
        this(stat, thenEffect, null, false);
    }

    public ReturnTargetPermanentsThenEffect(CardEffect thenEffect, PermanentPredicate targetFilter,
                                            boolean resolveThenEffectIfNoPermanentReturned) {
        this(EventStat.NONE, thenEffect, targetFilter, resolveThenEffectIfNoPermanentReturned);
    }

    @Override
    public TargetSpec targetSpec() {
        return targetFilter == null
                ? TargetSpec.benign(TargetPredicates.permanent())
                : TargetSpec.benign(TargetPredicates.permanents(targetFilter));
    }

    @Override
    public RemovalKind removalKind() {
        return RemovalKind.BOUNCE;
    }
}
