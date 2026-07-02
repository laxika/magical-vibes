package com.github.laxika.magicalvibes.model.condition;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/** The targeted permanent matches the predicate at resolution time. */
public record TargetPermanentMatches(PermanentPredicate filter) implements Condition {

    @Override
    public String conditionName() {
        return "target matches " + filter;
    }

    @Override
    public String conditionNotMetReason() {
        return "target does not match " + filter;
    }
}
