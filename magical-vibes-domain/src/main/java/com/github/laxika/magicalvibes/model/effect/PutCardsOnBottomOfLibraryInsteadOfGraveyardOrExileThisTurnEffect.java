package com.github.laxika.magicalvibes.model.effect;

/**
 * Replaces the controller's cards going to a graveyard or exile with putting them on the bottom
 * of their owner's library until end of turn.
 */
public record PutCardsOnBottomOfLibraryInsteadOfGraveyardOrExileThisTurnEffect() implements CardEffect {
}
