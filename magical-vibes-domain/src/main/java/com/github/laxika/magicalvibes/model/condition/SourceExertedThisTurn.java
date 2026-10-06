package com.github.laxika.magicalvibes.model.condition;

/** The source permanent has been exerted this turn (Combat Celebrant). */
public record SourceExertedThisTurn() implements Condition {

    @Override
    public String conditionName() {
        return "source exerted this turn";
    }

    @Override
    public String conditionNotMetReason() {
        return "source has not been exerted this turn";
    }
}
