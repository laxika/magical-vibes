package com.github.laxika.magicalvibes.model.condition;

/**
 * The controller of the effect's source had no cards in hand when their current turn began.
 */
public record ControllerHadNoCardsInHandAtTurnStart() implements Condition {

    @Override
    public String conditionName() {
        return "no cards in hand at the beginning of this turn";
    }

    @Override
    public String conditionNotMetReason() {
        return "you had a card in hand at the beginning of this turn";
    }
}
