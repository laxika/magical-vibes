package com.github.laxika.magicalvibes.model.condition;

/** The controller's life total is at or below the threshold. */
public record ControllerLifeAtMost(int threshold, boolean halfStartingLife) implements Condition {

    public ControllerLifeAtMost(int threshold) {
        this(threshold, false);
    }

    public static ControllerLifeAtMost atHalfStartingLife() {
        return new ControllerLifeAtMost(0, true);
    }

    @Override
    public String conditionName() {
        return halfStartingLife ? "life at or below half the starting life total"
                : "life at or below " + threshold;
    }

    @Override
    public String conditionNotMetReason() {
        return halfStartingLife ? "life total is greater than half the starting life total"
                : "life total is greater than " + threshold;
    }
}
