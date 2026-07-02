package com.github.laxika.magicalvibes.model.condition;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/** Any player controls a permanent matching the predicate. */
public record AnyPlayerControlsPermanent(PermanentPredicate filter) implements Condition {

    @Override
    public String conditionName() {
        return "any player controls a matching permanent";
    }

    @Override
    public String conditionNotMetReason() {
        return "no player controls a matching permanent";
    }
}
