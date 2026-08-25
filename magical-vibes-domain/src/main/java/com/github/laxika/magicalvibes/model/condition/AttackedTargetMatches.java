package com.github.laxika.magicalvibes.model.condition;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/** The permanent this creature attacked matches the predicate. */
public record AttackedTargetMatches(PermanentPredicate filter) implements Condition {

    @Override
    public String conditionName() {
        return "attacked target matches " + filter;
    }

    @Override
    public String conditionNotMetReason() {
        return "attacked target does not match " + filter;
    }
}
