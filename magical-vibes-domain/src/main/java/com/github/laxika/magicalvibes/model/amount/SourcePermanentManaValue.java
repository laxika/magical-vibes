package com.github.laxika.magicalvibes.model.amount;

/**
 * "Its mana value" of the ability's source permanent ("sacrifice that token unless you pay an amount of {E} equal to its
 * mana value"): the permanent as it is when the amount is read, else as it last existed on the battlefield (CR 608.2h);
 * a face-down permanent's is 0 (CR 708.2a). Read as the ability resolves, so a copy effect applied since it was made
 * changes it.
 */
public record SourcePermanentManaValue() implements DynamicAmount {
}
