package com.github.laxika.magicalvibes.model.effect;

/**
 * "You shuffle up to {@code maxCount} cards from your graveyard into your library." The cards are
 * chosen as the effect resolves (not targeted), so cards put into the graveyard earlier in the same
 * resolution (Stream of Thought's mill) can be chosen. Sibling of
 * {@link ShuffleTargetCardsFromControllerGraveyardIntoLibraryEffect}, which uses cast-time targets.
 */
public record ShuffleChosenCardsFromControllerGraveyardIntoLibraryEffect(int maxCount) implements CardEffect {
}
