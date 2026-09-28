package com.github.laxika.magicalvibes.model.effect;

/**
 * Each player mills one card. If a land was milled this way, the controller creates a Treasure,
 * then may cast one spell among the cards milled this way until end of turn.
 */
public record MugEffect() implements CardEffect {
}
