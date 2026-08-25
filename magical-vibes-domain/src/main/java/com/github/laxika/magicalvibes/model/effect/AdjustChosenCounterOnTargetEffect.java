package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;

/** Adjusts a chosen counter, or the fixed counter type, on a target permanent or suspended card. */
public record AdjustChosenCounterOnTargetEffect(boolean suspendedCardOwnedOnly, CounterType fixedCounterType)
        implements CardEffect {

    public AdjustChosenCounterOnTargetEffect() {
        this(false, null);
    }

    public AdjustChosenCounterOnTargetEffect(boolean suspendedCardOwnedOnly) {
        this(suspendedCardOwnedOnly, null);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.anyOf(
                TargetPredicates.permanent(), TargetPredicates.exileCard()),
                suspendedCardOwnedOnly ? new PermanentControlledBySourceControllerPredicate() : null);
    }

    @Override
    public boolean targetsAllExiledCardsInAbility() {
        return true;
    }
}
