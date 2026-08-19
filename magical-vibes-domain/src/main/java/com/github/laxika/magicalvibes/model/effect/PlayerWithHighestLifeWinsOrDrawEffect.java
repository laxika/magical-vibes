package com.github.laxika.magicalvibes.model.effect;

/**
 * The player with the highest life total wins the game. If multiple players are tied for the
 * highest life total, the game is a draw.
 */
public record PlayerWithHighestLifeWinsOrDrawEffect() implements CardEffect {
}
