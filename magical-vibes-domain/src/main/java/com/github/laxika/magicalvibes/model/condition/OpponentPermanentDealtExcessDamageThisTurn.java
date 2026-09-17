package com.github.laxika.magicalvibes.model.condition;

/** An opponent-controlled creature or planeswalker was dealt excess damage this turn. */
public record OpponentPermanentDealtExcessDamageThisTurn() implements Condition {

    @Override
    public String conditionName() {
        return "an opponent's creature or planeswalker was dealt excess damage this turn";
    }

    @Override
    public String conditionNotMetReason() {
        return "no opponent's creature or planeswalker was dealt excess damage this turn";
    }
}
