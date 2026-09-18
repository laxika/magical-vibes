package com.github.laxika.magicalvibes.model.condition;

/** A creature left the battlefield under the effect controller's control this turn. */
public record CreatureLeftBattlefieldUnderYourControlThisTurn() implements Condition {

    @Override
    public String conditionName() {
        return "a creature left the battlefield under your control this turn";
    }

    @Override
    public String conditionNotMetReason() {
        return "no creature left the battlefield under your control this turn";
    }
}
