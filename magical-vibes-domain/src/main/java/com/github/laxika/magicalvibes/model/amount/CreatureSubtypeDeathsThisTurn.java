package com.github.laxika.magicalvibes.model.amount;

import com.github.laxika.magicalvibes.model.CardSubtype;

/** The number of creatures with {@code subtype} that died this turn in the requested scope. */
public record CreatureSubtypeDeathsThisTurn(CardSubtype subtype, CountScope scope)
        implements DynamicAmount {
}
