package com.github.laxika.magicalvibes.model.condition;

/** A permanent was put into a graveyard from the battlefield this turn. */
public record PermanentPutIntoGraveyardFromBattlefieldThisTurn() implements Condition {

    @Override
    public String conditionName() {
        return "a permanent was put into a graveyard from the battlefield this turn";
    }

    @Override
    public String conditionNotMetReason() {
        return "no permanent was put into a graveyard from the battlefield this turn";
    }
}
