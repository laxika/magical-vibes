package com.github.laxika.magicalvibes.model.condition;

/** The controller's Ring has tempted them at least the given number of times. */
public record ControllerRingLevelAtLeast(int minimumLevel) implements Condition {

    @Override
    public String conditionName() {
        return "the Ring level";
    }

    @Override
    public String conditionNotMetReason() {
        return "the Ring has not tempted you enough times";
    }
}
