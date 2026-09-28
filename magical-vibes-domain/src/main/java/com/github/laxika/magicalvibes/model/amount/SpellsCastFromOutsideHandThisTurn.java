package com.github.laxika.magicalvibes.model.amount;

/** The number of spells cast this turn from somewhere other than the caster's hand. */
public record SpellsCastFromOutsideHandThisTurn(CountScope scope) implements DynamicAmount {
}
