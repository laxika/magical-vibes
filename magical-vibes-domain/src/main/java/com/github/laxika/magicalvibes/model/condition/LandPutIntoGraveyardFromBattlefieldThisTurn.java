package com.github.laxika.magicalvibes.model.condition;

/** A land controlled by the source's controller was put into a graveyard from the battlefield this turn. */
public record LandPutIntoGraveyardFromBattlefieldThisTurn() implements Condition {

    @Override
    public String conditionName() {
        return "a land you controlled was put into a graveyard from the battlefield this turn";
    }

    @Override
    public String conditionNotMetReason() {
        return "no land you controlled was put into a graveyard from the battlefield this turn";
    }
}
