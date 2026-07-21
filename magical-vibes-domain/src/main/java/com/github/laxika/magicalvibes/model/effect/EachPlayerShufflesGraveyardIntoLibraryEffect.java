package com.github.laxika.magicalvibes.model.effect;

/**
 * Each player shuffles their graveyard into their library.
 * Empty graveyards still cause that player's library to be shuffled (Survive ruling).
 */
public record EachPlayerShufflesGraveyardIntoLibraryEffect() implements CardEffect {
}
