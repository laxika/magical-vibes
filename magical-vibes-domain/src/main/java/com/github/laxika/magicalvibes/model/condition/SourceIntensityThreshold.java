package com.github.laxika.magicalvibes.model.condition;

/** The source permanent's persistent intensity is at least {@code threshold}. */
public record SourceIntensityThreshold(int threshold) implements Condition {

    @Override
    public String conditionName() {
        return "intensity threshold (" + threshold + "+)";
    }

    @Override
    public String conditionNotMetReason() {
        return "source has less than " + threshold + " intensity";
    }
}
