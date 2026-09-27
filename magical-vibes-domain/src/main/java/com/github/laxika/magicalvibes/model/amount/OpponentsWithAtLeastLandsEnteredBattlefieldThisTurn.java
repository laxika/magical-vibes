package com.github.laxika.magicalvibes.model.amount;

/** The number of opponents under whose control at least the specified number of lands entered this turn. */
public record OpponentsWithAtLeastLandsEnteredBattlefieldThisTurn(int minimum) implements DynamicAmount {
}
