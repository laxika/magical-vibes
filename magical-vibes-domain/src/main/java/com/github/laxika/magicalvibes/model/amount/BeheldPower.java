package com.github.laxika.magicalvibes.model.amount;

/**
 * The power of the Dragon used to pay a spell's optional behold cost. A battlefield choice uses
 * the chosen permanent's current power, falling back to its last-known power if it has left.
 */
public record BeheldPower() implements DynamicAmount {
}
