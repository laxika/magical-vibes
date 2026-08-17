package com.github.laxika.magicalvibes.model.condition;

/** The controller of the effect did not play a land this turn. */
public record DidntPlayLandThisTurn() implements Condition {

    @Override
    public String conditionName() {
        return "didn't play a land";
    }

    @Override
    public String conditionNotMetReason() {
        return "you played a land this turn";
    }
}
