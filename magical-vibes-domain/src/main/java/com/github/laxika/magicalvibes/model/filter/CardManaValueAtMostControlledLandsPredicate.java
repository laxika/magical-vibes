package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches a card whose mana value is at most the number of lands controlled by the perspective
 * player.
 */
public record CardManaValueAtMostControlledLandsPredicate() implements CardPredicate {
}
