package com.github.laxika.magicalvibes.model.effect;

/**
 * Looks at the top cards of the controller's library and may reveal a card that shares
 * a creature type with the source Equipment's equipped creature, putting the chosen card into the
 * controller's hand and the rest on the library bottom in a random order.
 *
 * @param count number of cards to look at from the top of the library
 */
public record LookAtTopCardsCreatureSharingTypeWithEquippedToHandEffect(int count)
        implements CardEffect {
}
