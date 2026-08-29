package com.github.laxika.magicalvibes.model.condition;

import com.github.laxika.magicalvibes.model.ManaValueParity;

/** True when the total number of counters on the source permanent has the requested parity. */
public record SourceCounterCountParity(ManaValueParity parity) implements Condition {

    @Override
    public String conditionName() {
        return "source counter count is " + parity.name().toLowerCase();
    }

    @Override
    public String conditionNotMetReason() {
        return "source counter count is not " + parity.name().toLowerCase();
    }
}
