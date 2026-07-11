package com.github.laxika.magicalvibes.model.condition;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/** One or more of the controller's attacking creatures match the predicate. */
public record HasAttacker(PermanentPredicate predicate) implements Condition {

    @Override
    public String conditionName() {
        return "matching attacker";
    }

    @Override
    public String conditionNotMetReason() {
        return "no matching attacker";
    }
}
