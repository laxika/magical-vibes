package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches cards whose power is less than or equal to the resolving spell or ability's X value.
 */
public record CardPowerAtMostXPredicate() implements CardPredicate {
}
