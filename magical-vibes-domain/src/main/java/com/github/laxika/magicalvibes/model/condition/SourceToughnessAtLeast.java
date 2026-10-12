package com.github.laxika.magicalvibes.model.condition;

/** True when the source permanent's effective toughness is at least {@code threshold}. */
public record SourceToughnessAtLeast(int threshold) implements Condition {

    @Override
    public String conditionName() {
        return "source's toughness is " + threshold + " or greater";
    }

    @Override
    public String conditionNotMetReason() {
        return "the source's toughness is less than " + threshold;
    }
}
