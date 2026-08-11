package com.github.laxika.magicalvibes.model.condition;

/**
 * At least one opponent has strictly more cards in hand than the controller.
 */
public record AnOpponentHasMoreCardsInHandThanController() implements Condition {

    @Override
    public String conditionName() {
        return "more cards in hand than you";
    }

    @Override
    public String conditionNotMetReason() {
        return "no opponent has more cards in hand than you";
    }
}
