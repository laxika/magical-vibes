package com.github.laxika.magicalvibes.model.condition;

import com.github.laxika.magicalvibes.model.CounterType;

/** The source permanent has at least {@code threshold} counters of the given type. */
public record SourceCounterThreshold(int threshold, CounterType counterType) implements Condition {

    @Override
    public String conditionName() {
        return counterType.name().toLowerCase() + " counter threshold (" + threshold + "+)";
    }

    @Override
    public String conditionNotMetReason() {
        return "fewer than " + threshold + " " + counterType.name().toLowerCase() + " counters on source";
    }
}
