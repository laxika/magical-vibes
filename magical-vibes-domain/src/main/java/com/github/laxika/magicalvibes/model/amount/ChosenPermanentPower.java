package com.github.laxika.magicalvibes.model.amount;

/**
 * The effective power (never negative) of the permanent recorded as chosen on the current stack
 * entry. Evaluated at resolution from the live permanent and evaluates to 0 when it has left the
 * battlefield or none was recorded.
 */
public record ChosenPermanentPower() implements DynamicAmount {
}
