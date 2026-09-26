package com.github.laxika.magicalvibes.model.condition;

/** True when the targeted player is the active player. */
public record TargetPlayerIsActive() implements Condition {

    @Override
    public String conditionName() {
        return "the targeted player's turn";
    }

    @Override
    public String conditionNotMetReason() {
        return "it is not the targeted player's turn";
    }
}
