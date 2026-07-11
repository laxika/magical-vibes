package com.github.laxika.magicalvibes.model.amount;

/** The number of cards in the hand(s) of the players in scope. */
public record CardsInHand(CountScope scope) implements DynamicAmount {
}
