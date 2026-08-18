package com.github.laxika.magicalvibes.model.condition;

/** True when exactly one player has more cards in hand than every other player. */
public record APlayerHasMoreCardsInHandThanEachOtherPlayer() implements Condition {

    @Override
    public String conditionName() {
        return "a player has more cards in hand than each other player";
    }

    @Override
    public String conditionNotMetReason() {
        return "no player has more cards in hand than each other player";
    }
}
