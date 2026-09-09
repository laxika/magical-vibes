package com.github.laxika.magicalvibes.model.effect;

/**
 * Looks at the top {@code count} cards of the controller's library, then may reveal up to two
 * creature and/or land cards from among them. Selected lands go onto the battlefield tapped,
 * selected nonland creatures go into the controller's hand, and all other looked-at cards go on
 * the bottom of the library in a random order.
 */
public record LookAtTopCardsChooseCreaturesAndLandsEffect(int count) implements CardEffect {
}
