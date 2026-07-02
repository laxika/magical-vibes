package com.github.laxika.magicalvibes.model.condition;

/** It is the source permanent's controller's turn. */
public record ControllerTurn() implements Condition {

    @Override
    public String conditionName() {
        return "controller's turn";
    }

    @Override
    public String conditionNotMetReason() {
        return "not controller's turn";
    }
}
