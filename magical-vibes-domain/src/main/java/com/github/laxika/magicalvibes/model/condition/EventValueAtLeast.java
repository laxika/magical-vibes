package com.github.laxika.magicalvibes.model.condition;

/**
 * The current event value is at least {@code threshold}. Used for triggers whose event amount
 * is recorded on the triggered stack entry.
 */
public record EventValueAtLeast(int threshold) implements Condition {

    @Override
    public String conditionName() {
        return threshold + " or more damage was dealt";
    }

    @Override
    public String conditionNotMetReason() {
        return "less than " + threshold + " damage was dealt";
    }
}
