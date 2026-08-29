package com.github.laxika.magicalvibes.model.condition;

/** True when the defending player has more cards in hand than the controller. */
public record DefendingPlayerHasMoreCardsInHandThanController() implements Condition {

    @Override
    public String conditionName() {
        return "defending player has more cards in hand than you";
    }

    @Override
    public String conditionNotMetReason() {
        return "defending player does not have more cards in hand than you";
    }
}
