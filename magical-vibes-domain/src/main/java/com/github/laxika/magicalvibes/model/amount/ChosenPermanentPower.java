package com.github.laxika.magicalvibes.model.amount;

/**
 * The effective power (never negative) of the permanent recorded as chosen on the current stack
 * entry. Evaluated at resolution from the live permanent, with a captured entering-permanent power
 * fallback when that permanent has left the battlefield.
 */
public record ChosenPermanentPower() implements DynamicAmount {
}
