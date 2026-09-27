package com.github.laxika.magicalvibes.model.amount;

/** The number of opponents who drew at least the specified number of cards this turn. */
public record OpponentsWithAtLeastCardsDrawnThisTurn(int minimum) implements DynamicAmount {
}
