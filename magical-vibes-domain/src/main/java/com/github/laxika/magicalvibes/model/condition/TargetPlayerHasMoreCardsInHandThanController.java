package com.github.laxika.magicalvibes.model.condition;

/**
 * True when the player targeted by the spell or ability has strictly more cards in hand than its
 * controller.
 */
public record TargetPlayerHasMoreCardsInHandThanController() implements Condition {

    @Override
    public String conditionName() {
        return "more cards in hand than you";
    }

    @Override
    public String conditionNotMetReason() {
        return "that player does not have more cards in hand than you";
    }
}
