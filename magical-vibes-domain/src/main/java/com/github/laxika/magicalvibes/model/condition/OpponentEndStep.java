package com.github.laxika.magicalvibes.model.condition;

/** It is an end step of a turn that isn't the source controller's turn. */
public record OpponentEndStep() implements Condition {

    @Override
    public String conditionName() {
        return "opponent's end step";
    }

    @Override
    public String conditionNotMetReason() {
        return "not opponent's end step";
    }
}
