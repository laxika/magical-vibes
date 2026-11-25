package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;

/**
 * Adjusts a chosen counter, or the fixed counter type, on a target permanent or suspended card: "remove that counter
 * or put another of those counters on it". {@code optional} is "you may" (Jhoira's Timebug): the controller may do
 * neither.
 */
public record AdjustChosenCounterOnTargetEffect(boolean suspendedCardOwnedOnly, CounterType fixedCounterType,
                                                boolean optional)
        implements CardEffect {

    public AdjustChosenCounterOnTargetEffect() {
        this(false, null, false);
    }

    public AdjustChosenCounterOnTargetEffect(boolean suspendedCardOwnedOnly) {
        this(suspendedCardOwnedOnly, null, false);
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
