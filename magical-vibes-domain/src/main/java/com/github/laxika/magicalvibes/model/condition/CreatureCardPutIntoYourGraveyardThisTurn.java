package com.github.laxika.magicalvibes.model.condition;

/** A creature card was put into the controller's graveyard from anywhere this turn. */
public record CreatureCardPutIntoYourGraveyardThisTurn() implements Condition {

    @Override
    public String conditionName() {
        return "a creature card was put into your graveyard from anywhere this turn";
    }

    @Override
    public String conditionNotMetReason() {
        return "no creature card was put into your graveyard from anywhere this turn";
    }
}
