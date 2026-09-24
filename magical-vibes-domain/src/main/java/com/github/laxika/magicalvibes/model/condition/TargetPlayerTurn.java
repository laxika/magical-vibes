package com.github.laxika.magicalvibes.model.condition;

/** The targeted player's turn is currently in progress. */
public record TargetPlayerTurn() implements Condition {

    @Override
    public String conditionName() {
        return "the targeted player's turn";
    }

    @Override
    public String conditionNotMetReason() {
        return "it is not the targeted player's turn";
    }
}
