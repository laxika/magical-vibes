package com.github.laxika.magicalvibes.model.condition;

/** At least one face-down creature entered the battlefield under the controller's control this turn. */
public record FaceDownCreatureEnteredThisTurn() implements Condition {

    @Override
    public String conditionName() {
        return "a face-down creature entered this turn";
    }

    @Override
    public String conditionNotMetReason() {
        return "no face-down creature entered the battlefield this turn";
    }
}
