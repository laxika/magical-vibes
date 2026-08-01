package com.github.laxika.magicalvibes.model.amount;

/**
 * The mana value of the permanent the source Aura is attached to, read at resolution time
 * (0 when the source is not on the battlefield or is attached to nothing). Used by Soul Tithe's
 * "that player sacrifices it unless they pay {X}, where X is its mana value".
 */
public record EnchantedPermanentManaValue() implements DynamicAmount {
}
