package com.github.laxika.magicalvibes.model.effect;

/**
 * Each player reveals up to {@code count} cards from the top of their library, puts all revealed
 * land cards onto the battlefield tapped, and exiles the rest.
 */
public record EachPlayerRevealsTopCardsLandsToBattlefieldTappedRestExiledEffect(int count)
        implements CardEffect {
}
