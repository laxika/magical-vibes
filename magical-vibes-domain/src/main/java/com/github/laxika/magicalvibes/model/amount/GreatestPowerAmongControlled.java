package com.github.laxika.magicalvibes.model.amount;

/**
 * The greatest effective power among creatures the controller controls (0 when the
 * controller has no creatures; negative powers never lower the result below 0).
 */
public record GreatestPowerAmongControlled() implements DynamicAmount {
}
