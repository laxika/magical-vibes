package com.github.laxika.magicalvibes.model.effect;

/**
 * Each player who controls fewer lands than the player who controls the most lands may search
 * their library for up to the difference in basic land cards, reveal them, and put them into their
 * hand, then shuffle.
 */
public record EachPlayerMaySearchLibraryForBasicLandsToHandEffect() implements CardEffect {
}
