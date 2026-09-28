package com.github.laxika.magicalvibes.model.effect;

/**
 * Each player who controls fewer lands than the player with the most lands may search their
 * library for up to the difference in basic land cards and put them onto the battlefield tapped.
 */
public record EachPlayerMaySearchLibraryForBasicLandsToMatchMostLandsEffect() implements CardEffect {
}
