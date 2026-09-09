package com.github.laxika.magicalvibes.model.effect;

/**
 * Chooses a creature type, then shuffles all matching creature cards from the controller's
 * graveyard into their library. Changeling cards match every creature type.
 */
public record ShuffleGraveyardOfChosenCreatureTypeIntoLibraryEffect() implements CardEffect {
}
