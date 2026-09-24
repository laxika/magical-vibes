package com.github.laxika.magicalvibes.model.effect;

/**
 * Chooses a creature type, then looks at the top cards of the controller's library. The
 * controller may reveal any number of cards of that type into their hand; the rest go to the
 * bottom of the library in a random order.
 */
public record LookAtTopCardsOfChosenCreatureTypeToHandRestRandomEffect(int count)
        implements CardEffect {
}
