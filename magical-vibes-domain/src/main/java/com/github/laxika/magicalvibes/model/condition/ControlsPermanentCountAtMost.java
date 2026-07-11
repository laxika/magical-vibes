package com.github.laxika.magicalvibes.model.condition;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/** The controller controls at most {@code maxCount} permanents matching the predicate. */
public record ControlsPermanentCountAtMost(int maxCount, PermanentPredicate filter) implements Condition {

    @Override
    public String conditionName() {
        return "controls " + maxCount + " or fewer matching permanents";
    }

    @Override
    public String conditionNotMetReason() {
        return "controller controls more than " + maxCount + " matching permanents";
    }
}
