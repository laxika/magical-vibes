package com.github.laxika.magicalvibes.model.condition;

/**
 * At least one opponent has the required number of additional cards in hand compared to the
 * controller.
 */
public record AnOpponentHasMoreCardsInHandThanController(int minimumDifference) implements Condition {

    public AnOpponentHasMoreCardsInHandThanController() {
        this(1);
    }

    @Override
    public String conditionName() {
        return minimumDifference == 1
                ? "more cards in hand than you"
                : "at least " + minimumDifference + " more cards in hand than you";
    }

    @Override
    public String conditionNotMetReason() {
        return minimumDifference == 1
                ? "no opponent has more cards in hand than you"
                : "no opponent has at least " + minimumDifference + " more cards in hand than you";
    }
}
