package com.github.laxika.magicalvibes.model.amount;

/**
 * The mana value of the permanent whose death caused the resolving delayed trigger.
 * The value is captured from the permanent's last-known battlefield characteristics.
 */
public record DyingPermanentManaValue() implements DynamicAmount {
}
