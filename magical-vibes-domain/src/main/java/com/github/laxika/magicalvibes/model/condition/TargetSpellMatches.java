package com.github.laxika.magicalvibes.model.condition;

import com.github.laxika.magicalvibes.model.filter.StackEntryPredicate;

/** The targeted spell on the stack matches the predicate at resolution time. */
public record TargetSpellMatches(StackEntryPredicate filter) implements Condition {

    @Override
    public String conditionName() {
        return "target spell matches " + filter;
    }

    @Override
    public String conditionNotMetReason() {
        return "target spell does not match " + filter;
    }
}
