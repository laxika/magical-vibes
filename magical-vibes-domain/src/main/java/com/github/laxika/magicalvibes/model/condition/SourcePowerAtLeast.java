package com.github.laxika.magicalvibes.model.condition;

/** The source permanent has effective power {@code threshold} or greater. */
public record SourcePowerAtLeast(int threshold) implements Condition {

    @Override
    public String conditionName() {
        return "source's power is " + threshold + " or greater";
    }

    @Override
    public String conditionNotMetReason() {
        return "the source's power is less than " + threshold;
    }
}
