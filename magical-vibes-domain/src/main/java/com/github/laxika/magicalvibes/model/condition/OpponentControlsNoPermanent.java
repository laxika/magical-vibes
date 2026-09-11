package com.github.laxika.magicalvibes.model.condition;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/** True when at least one opponent of the controller controls no permanent matching the predicate. */
public record OpponentControlsNoPermanent(PermanentPredicate filter) implements Condition {

    @Override
    public String conditionName() {
        return "an opponent controls no matching permanent";
    }

    @Override
    public String conditionNotMetReason() {
        return "every opponent controls a matching permanent";
    }
}
