package com.github.laxika.magicalvibes.model.amount;

/** The number of players in a scope whose hand has at least the threshold number of cards. */
public record PlayersWithCardsInHandAtLeast(CountScope scope, int threshold) implements DynamicAmount {
}
