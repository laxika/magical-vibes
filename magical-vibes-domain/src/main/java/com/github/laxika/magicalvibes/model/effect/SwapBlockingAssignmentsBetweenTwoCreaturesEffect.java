package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentIsBlockingPredicate;

/**
 * Sorrow's Path: exchanges the creatures blocked by two target blocking creatures when both
 * creatures can legally block the other's entire group. Each blocker blocks again, so its
 * "whenever this blocks" and "whenever a creature blocks [that attacker]" abilities trigger again;
 * the attackers never stopped being blocked, so their "becomes blocked" abilities don't (ruling).
 */
public record SwapBlockingAssignmentsBetweenTwoCreaturesEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature(), new PermanentIsBlockingPredicate());
    }
}
