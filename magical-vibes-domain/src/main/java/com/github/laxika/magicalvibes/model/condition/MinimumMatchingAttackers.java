package com.github.laxika.magicalvibes.model.condition;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/** At least {@code minimum} of the controller's attacking permanents match the predicate. */
public record MinimumMatchingAttackers(int minimum, PermanentPredicate predicate) implements Condition {

    @Override
    public String conditionName() {
        return "at least " + minimum + " matching attackers";
    }

    @Override
    public String conditionNotMetReason() {
        return "fewer than " + minimum + " matching attackers";
    }
}
