package com.github.laxika.magicalvibes.model.effect;

/**
 * Each player exiles the top card of their library and the player whose card has the greatest
 * mana value takes an extra turn. Tied players repeat the process until a single winner remains.
 */
public record TimesifterEffect() implements CardEffect {
}
