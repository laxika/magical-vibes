package com.github.laxika.magicalvibes.model.amount;

import com.github.laxika.magicalvibes.model.CardSubtype;

/**
 * The total power of the creatures with {@code subtype} that died this turn in the requested scope, each with the
 * power and subtypes it last had on the battlefield ("the total power of Daleks that died this turn").
 */
public record CreatureSubtypeDeathPowerThisTurn(CardSubtype subtype, CountScope scope)
        implements DynamicAmount {
}
