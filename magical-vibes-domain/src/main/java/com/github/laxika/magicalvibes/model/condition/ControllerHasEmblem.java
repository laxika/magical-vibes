package com.github.laxika.magicalvibes.model.condition;

/** The controller has one or more emblems. */
public record ControllerHasEmblem() implements Condition {

    @Override
    public String conditionName() {
        return "an emblem";
    }

    @Override
    public String conditionNotMetReason() {
        return "the controller does not have an emblem";
    }
}
