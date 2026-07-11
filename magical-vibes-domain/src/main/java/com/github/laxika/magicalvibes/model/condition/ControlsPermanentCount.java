package com.github.laxika.magicalvibes.model.condition;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/** The controller controls at least {@code minCount} permanents matching the predicate. */
public record ControlsPermanentCount(int minCount, PermanentPredicate filter) implements Condition {

    @Override
    public String conditionName() {
        return "controls " + minCount + " or more matching permanents";
    }

    @Override
    public String conditionNotMetReason() {
        return "controller controls fewer than " + minCount + " matching permanents";
    }
}
