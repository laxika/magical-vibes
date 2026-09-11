package com.github.laxika.magicalvibes.model.effect;

/**
 * Reveals cards from the top of the controller's library until a creature card is revealed. If
 * its mana value is at most the controller's land count, it enters the battlefield; otherwise it
 * goes into the controller's hand. All other revealed cards go on the bottom in a random order.
 */
public record RevealUntilCreatureToBattlefieldOrHandByManaValueEffect() implements CardEffect {
}
