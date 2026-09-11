package com.github.laxika.magicalvibes.model.condition;

/** The controller has the enduring story designation. */
public record ControllerHasEnduringStory() implements Condition {

    @Override
    public String conditionName() {
        return "an enduring story";
    }

    @Override
    public String conditionNotMetReason() {
        return "the controller does not have an enduring story";
    }
}
