package com.github.laxika.magicalvibes.model.condition;

/**
 * True when no player has any cards in hand (Howltooth Hollow's "if each player has no cards
 * in hand"). Evaluated across every player, not just the controller.
 */
public record NoPlayerHasCardsInHand() implements Condition {

    @Override
    public String conditionName() {
        return "each player has no cards in hand";
    }

    @Override
    public String conditionNotMetReason() {
        return "a player still has cards in hand";
    }
}
