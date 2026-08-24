package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches cards whose mana value is at most the resolving spell's X value.
 */
public record CardMaxManaValueXPredicate() implements CardPredicate {
}
