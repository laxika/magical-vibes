package com.github.laxika.magicalvibes.model.condition;

/** True when controlled creatures have at least the given number of distinct counter kinds. */
public record ControlledCreatureCounterKindsAtLeast(int threshold) implements Condition {

    @Override
    public String conditionName() {
        return "creatures you control have at least " + threshold + " different counter kinds";
    }

    @Override
    public String conditionNotMetReason() {
        return "creatures you control have fewer than " + threshold + " different counter kinds";
    }
}
