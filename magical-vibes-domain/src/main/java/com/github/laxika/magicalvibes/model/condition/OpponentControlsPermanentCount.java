package com.github.laxika.magicalvibes.model.condition;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/** An opponent of the controller controls at least {@code minCount} permanents matching the filter. */
public record OpponentControlsPermanentCount(int minCount, PermanentPredicate filter) implements Condition {

    @Override
    public String conditionName() {
        return "an opponent controls " + minCount + " or more matching permanents";
    }

    @Override
    public String conditionNotMetReason() {
        return "no opponent controls enough matching permanents";
    }
}
