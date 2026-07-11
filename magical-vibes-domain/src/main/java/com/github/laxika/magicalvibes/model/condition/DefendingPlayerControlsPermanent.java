package com.github.laxika.magicalvibes.model.condition;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/** The defending player (opponent of the controller) controls a permanent matching the predicate. */
public record DefendingPlayerControlsPermanent(PermanentPredicate filter) implements Condition {

    @Override
    public String conditionName() {
        return "defending player controls a matching permanent";
    }

    @Override
    public String conditionNotMetReason() {
        return "defending player controls no matching permanent";
    }
}
