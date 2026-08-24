package com.github.laxika.magicalvibes.model.effect;

/**
 * As this creature enters, turns all other nontoken creatures face down as 2/2 creatures.
 * This replacement is handled during permanent entry before enter-the-battlefield abilities are
 * collected. Double-faced cards remain face up because they cannot be turned face down.
 */
public record TurnOtherNontokenCreaturesFaceDownOnEnterEffect() implements ReplacementEffect {
}
