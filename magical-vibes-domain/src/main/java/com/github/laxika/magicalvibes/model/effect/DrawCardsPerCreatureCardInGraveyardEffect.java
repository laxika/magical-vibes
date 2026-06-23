package com.github.laxika.magicalvibes.model.effect;

/**
 * Draws a configurable amount of cards for each creature card in the controller's graveyard.
 *
 * @param cardsPerCreature number of cards drawn per creature card
 */
public record DrawCardsPerCreatureCardInGraveyardEffect(int cardsPerCreature) implements CardEffect {
}
