package com.github.laxika.magicalvibes.model.condition;

/** At least one face-down permanent entered the battlefield under the controller's control this turn. */
public record FaceDownPermanentEnteredThisTurn() implements Condition {

    @Override
    public String conditionName() {
        return "a face-down permanent entered this turn";
    }

    @Override
    public String conditionNotMetReason() {
        return "no face-down permanent entered the battlefield this turn";
    }
}
