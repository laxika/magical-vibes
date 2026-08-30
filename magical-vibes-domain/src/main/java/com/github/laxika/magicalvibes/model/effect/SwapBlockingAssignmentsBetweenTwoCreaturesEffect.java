package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentIsBlockingPredicate;

/**
 * Sorrow's Path: exchanges the creatures blocked by two target blocking creatures when both
 * creatures can legally block the other's entire group. The assignment is changed directly, so
 * the creatures do not become blocked again and block triggers do not fire again.
 */
public record SwapBlockingAssignmentsBetweenTwoCreaturesEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature(), new PermanentIsBlockingPredicate());
    }
}
