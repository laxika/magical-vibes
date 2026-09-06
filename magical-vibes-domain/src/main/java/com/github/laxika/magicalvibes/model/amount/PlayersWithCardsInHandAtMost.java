package com.github.laxika.magicalvibes.model.amount;

/** The number of players in scope with at most {@code threshold} cards in hand. */
public record PlayersWithCardsInHandAtMost(CountScope scope, int threshold) implements DynamicAmount {
}
