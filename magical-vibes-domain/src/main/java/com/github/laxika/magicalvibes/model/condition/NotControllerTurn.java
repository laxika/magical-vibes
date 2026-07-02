package com.github.laxika.magicalvibes.model.condition;

/** It is a turn other than the source permanent's controller's turn. */
public record NotControllerTurn() implements Condition {

    @Override
    public String conditionName() {
        return "turns other than controller's";
    }

    @Override
    public String conditionNotMetReason() {
        return "controller's turn";
    }
}
