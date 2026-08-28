package com.github.laxika.magicalvibes.model.condition;

/** A creature died under an opponent's control this turn. */
public record CreatureDiedUnderOpponentControlThisTurn() implements Condition {

    @Override
    public String conditionName() {
        return "a creature died under an opponent's control this turn";
    }

    @Override
    public String conditionNotMetReason() {
        return "no creature died under an opponent's control this turn";
    }
}
