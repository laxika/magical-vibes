package com.github.laxika.magicalvibes.model.condition;

/** The controller's life total is at or above the threshold. */
public record ControllerLifeAtLeast(int threshold, boolean relativeToStartingLifeTotal) implements Condition {

    public ControllerLifeAtLeast(int threshold) {
        this(threshold, false);
    }

    @Override
    public String conditionName() {
        return "life threshold (" + threshold + "+)";
    }

    @Override
    public String conditionNotMetReason() {
        return "life total is less than " + threshold;
    }
}
