package com.github.laxika.magicalvibes.model.effect;

/**
 * Each player may exile any number of cards from their graveyard. After all choices, each player
 * loses life equal to the number of cards remaining in their graveyard.
 */
public record EachPlayerMayExileGraveyardCardsThenLoseLifeEffect() implements CardEffect {
}
