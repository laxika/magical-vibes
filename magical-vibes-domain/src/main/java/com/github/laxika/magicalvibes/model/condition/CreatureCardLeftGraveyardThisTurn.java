package com.github.laxika.magicalvibes.model.condition;

/** A creature card left the controller's graveyard this turn. */
public record CreatureCardLeftGraveyardThisTurn() implements Condition {

    @Override
    public String conditionName() {
        return "a creature card left your graveyard this turn";
    }

    @Override
    public String conditionNotMetReason() {
        return "no creature card left your graveyard this turn";
    }
}
