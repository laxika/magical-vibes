package com.github.laxika.magicalvibes.model.effect;

/**
 * Put up to {@code maxCount} cards from the controller's graveyard on top of their library in an
 * order of their choosing. When fewer matching cards are available, all of them are put back.
 */
public record PutCardsFromControllerGraveyardOnTopOfLibraryEffect(int maxCount) implements CardEffect {
}
