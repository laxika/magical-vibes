package com.github.laxika.magicalvibes.model.condition;

/** True when the player stored in the target slot is the active player. */
public record TargetPlayerIsActivePlayer() implements Condition {

    @Override
    public String conditionName() {
        return "that player's turn";
    }

    @Override
    public String conditionNotMetReason() {
        return "it is not that player's turn";
    }
}
