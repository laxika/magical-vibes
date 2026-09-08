package com.github.laxika.magicalvibes.model.effect;

/**
 * Each opponent may search their library for a land card, put it onto the battlefield, then
 * shuffle. Opponents search in APNAP order (active player first among opponents).
 *
 * <p>Used by Hired Giant.
 */
public record EachOpponentMaySearchLibraryForLandToBattlefieldEffect() implements CardEffect {
}
