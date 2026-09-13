package com.github.laxika.magicalvibes.model.filter;

/** Matches cards whose mana value is strictly less than the resolving spell or ability's X value. */
public record CardManaValueLessThanXPredicate() implements CardPredicate {
}
