package com.github.laxika.magicalvibes.model.effect;

/**
 * Each player chooses a card name, then each player reveals the top card of their library.
 * If the revealed card matches the chosen name, it goes to that player's hand.
 * If it doesn't match, it goes on the bottom of that player's library.
 * Used by Conundrum Sphinx.
 */
public record EachPlayerNameCardRevealTopEffect() implements CardEffect {
}
