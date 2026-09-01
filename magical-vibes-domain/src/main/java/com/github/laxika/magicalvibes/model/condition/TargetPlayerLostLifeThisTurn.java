package com.github.laxika.magicalvibes.model.condition;

/** True when the player targeted by the spell or ability has lost life this turn. */
public record TargetPlayerLostLifeThisTurn() implements Condition {

    @Override
    public String conditionName() {
        return "that player lost life this turn";
    }

    @Override
    public String conditionNotMetReason() {
        return "that player has not lost life this turn";
    }
}
