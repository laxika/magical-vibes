package com.github.laxika.magicalvibes.model.effect;

/**
 * Starting with the effect controller, each player votes for dominion or guidance. Dominion
 * wins only with a strict majority; otherwise the controller draws a card.
 */
public record GaladrielElvenQueenEffect() implements CardEffect {
}
