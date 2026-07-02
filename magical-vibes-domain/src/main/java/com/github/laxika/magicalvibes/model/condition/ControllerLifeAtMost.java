package com.github.laxika.magicalvibes.model.condition;

/** The controller's life total is at or below the threshold. */
public record ControllerLifeAtMost(int threshold) implements Condition {

    @Override
    public String conditionName() {
        return "life at or below " + threshold;
    }

    @Override
    public String conditionNotMetReason() {
        return "life total is greater than " + threshold;
    }
}
