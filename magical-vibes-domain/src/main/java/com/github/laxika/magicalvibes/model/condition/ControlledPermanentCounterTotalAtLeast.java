package com.github.laxika.magicalvibes.model.condition;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/** True when matching permanents controlled by the controller have enough counters in total. */
public record ControlledPermanentCounterTotalAtLeast(
        int threshold, CounterType counterType, PermanentPredicate filter) implements Condition {

    @Override
    public String conditionName() {
        return "matching permanents you control have at least " + threshold + " counters";
    }

    @Override
    public String conditionNotMetReason() {
        return "matching permanents you control have fewer than " + threshold + " counters";
    }
}
