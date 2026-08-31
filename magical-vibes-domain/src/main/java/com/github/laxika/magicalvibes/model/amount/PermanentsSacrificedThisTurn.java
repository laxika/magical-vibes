package com.github.laxika.magicalvibes.model.amount;

/** The number of permanents sacrificed by players in the given scope this turn. */
public record PermanentsSacrificedThisTurn(CountScope scope) implements DynamicAmount {
}
