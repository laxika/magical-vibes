package com.github.laxika.magicalvibes.model.condition;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/** True when at least one player controls no permanent matching the predicate. */
public record AnyPlayerControlsNoPermanent(PermanentPredicate filter) implements Condition {

    @Override
    public String conditionName() {
        return "a player controls no matching permanent";
    }

    @Override
    public String conditionNotMetReason() {
        return "every player controls a matching permanent";
    }
}
