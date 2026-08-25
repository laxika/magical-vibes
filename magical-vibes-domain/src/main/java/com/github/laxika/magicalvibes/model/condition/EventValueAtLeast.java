package com.github.laxika.magicalvibes.model.condition;

/** The snapshotted value carried by the triggering event is at least the specified amount. */
public record EventValueAtLeast(int minimum) implements Condition {

    @Override
    public String conditionName() {
        return "event value is at least " + minimum;
    }

    @Override
    public String conditionNotMetReason() {
        return "event value is less than " + minimum;
    }
}
