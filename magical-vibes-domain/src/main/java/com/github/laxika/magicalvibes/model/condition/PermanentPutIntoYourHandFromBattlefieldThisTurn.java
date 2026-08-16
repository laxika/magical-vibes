package com.github.laxika.magicalvibes.model.condition;

/** A permanent was put into the controller's hand from the battlefield this turn. */
public record PermanentPutIntoYourHandFromBattlefieldThisTurn() implements Condition {

    @Override
    public String conditionName() {
        return "a permanent was put into your hand from the battlefield this turn";
    }

    @Override
    public String conditionNotMetReason() {
        return "no permanent was put into your hand from the battlefield this turn";
    }
}
