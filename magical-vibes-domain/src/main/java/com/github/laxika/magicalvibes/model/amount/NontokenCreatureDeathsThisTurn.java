package com.github.laxika.magicalvibes.model.amount;

/** The number of nontoken creatures that died this turn in the requested scope. */
public record NontokenCreatureDeathsThisTurn(CountScope scope) implements DynamicAmount {
}
