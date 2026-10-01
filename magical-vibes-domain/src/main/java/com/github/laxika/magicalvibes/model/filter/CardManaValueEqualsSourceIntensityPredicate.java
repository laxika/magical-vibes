package com.github.laxika.magicalvibes.model.filter;

/** Matches cards whose mana value equals the source card's current intensity. */
public record CardManaValueEqualsSourceIntensityPredicate() implements CardPredicate {
}
