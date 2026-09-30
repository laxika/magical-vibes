package com.github.laxika.magicalvibes.model.filter;

/** Matches cards whose mana value is strictly greater than the perspective player's hand size. */
public record CardManaValueGreaterThanControllerHandSizePredicate() implements CardPredicate {
}
