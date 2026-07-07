package com.github.laxika.magicalvibes.model.condition;

/** An opponent of the controller has been dealt damage this turn (from any source). */
public record OpponentDealtDamageThisTurn() implements Condition {

    @Override
    public String conditionName() {
        return "an opponent was dealt damage this turn";
    }

    @Override
    public String conditionNotMetReason() {
        return "no opponent was dealt damage this turn";
    }
}
