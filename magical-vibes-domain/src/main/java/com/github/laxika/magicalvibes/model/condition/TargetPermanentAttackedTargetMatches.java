package com.github.laxika.magicalvibes.model.condition;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/** The targeted permanent's attack target matches the predicate. */
public record TargetPermanentAttackedTargetMatches(PermanentPredicate filter) implements Condition {

    @Override
    public String conditionName() {
        return "target's attacked target matches " + filter;
    }

    @Override
    public String conditionNotMetReason() {
        return "target's attacked target does not match " + filter;
    }
}
