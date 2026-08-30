package com.github.laxika.magicalvibes.model.condition;

/** True when the source permanent's effective power is at least {@code threshold}. */
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
