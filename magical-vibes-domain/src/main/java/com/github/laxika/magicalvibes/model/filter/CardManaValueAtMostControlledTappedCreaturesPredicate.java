package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches cards whose mana value is at most the number of tapped creatures controlled by the
 * perspective player.
 */
public record CardManaValueAtMostControlledTappedCreaturesPredicate() implements CardPredicate {
}
