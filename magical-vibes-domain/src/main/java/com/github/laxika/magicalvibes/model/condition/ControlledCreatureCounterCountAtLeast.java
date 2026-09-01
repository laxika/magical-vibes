package com.github.laxika.magicalvibes.model.condition;

/** True when creatures controlled by the controller have at least the given number of counters in total. */
public record ControlledCreatureCounterCountAtLeast(int threshold) implements Condition {

    @Override
    public String conditionName() {
        return "creatures you control have at least " + threshold + " counters";
    }

    @Override
    public String conditionNotMetReason() {
        return "creatures you control have fewer than " + threshold + " counters";
    }
}
