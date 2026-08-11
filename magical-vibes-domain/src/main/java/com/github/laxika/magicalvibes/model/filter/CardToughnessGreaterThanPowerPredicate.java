package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches a card whose printed toughness is greater than its printed power.
 * Cards without either characteristic never match.
 */
public record CardToughnessGreaterThanPowerPredicate() implements CardPredicate {
}
