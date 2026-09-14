package com.github.laxika.magicalvibes.model.effect;

/**
 * On resolution, chooses land or nonland, then reveals until a card of the chosen kind is found.
 * The found card goes into its controller's hand and the other revealed cards go on the bottom of
 * the library in a random order.
 */
public record RevealUntilChosenLandOrNonlandToHandEffect() implements CardEffect {
}
