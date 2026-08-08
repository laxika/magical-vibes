package com.github.laxika.magicalvibes.model.condition;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/** The controller controls at least {@code minCount} matching permanents with different names. */
public record ControlsDistinctPermanentNamesCount(int minCount, PermanentPredicate filter) implements Condition {

    @Override
    public String conditionName() {
        return "controls " + minCount + " or more matching permanents with different names";
    }

    @Override
    public String conditionNotMetReason() {
        return "controller controls fewer than " + minCount + " matching permanents with different names";
    }

    @Override
    public boolean isEtbTriggerGate() {
        return true;
    }
}
