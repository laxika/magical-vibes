package com.github.laxika.magicalvibes.model.condition;

/**
 * One or more cards left the controller's graveyard this turn (e.g. Wilt in the Heat:
 * "if one or more cards left your graveyard this turn").
 */
public record CardsLeftGraveyardThisTurn() implements Condition {

    @Override
    public String conditionName() {
        return "a card left your graveyard this turn";
    }

    @Override
    public String conditionNotMetReason() {
        return "no card left your graveyard this turn";
    }
}
