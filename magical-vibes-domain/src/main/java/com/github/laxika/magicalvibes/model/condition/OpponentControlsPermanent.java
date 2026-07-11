package com.github.laxika.magicalvibes.model.condition;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/** An opponent of the controller controls a permanent matching the predicate. */
public record OpponentControlsPermanent(PermanentPredicate filter) implements Condition {

    @Override
    public String conditionName() {
        return "opponent controls a matching permanent";
    }

    @Override
    public String conditionNotMetReason() {
        return "no opponent controls a matching permanent";
    }
}
