package com.github.laxika.magicalvibes.model.amount;

/** The number of spells cast this turn by the players in scope. */
public record SpellsCastThisTurn(CountScope scope) implements DynamicAmount {
}
