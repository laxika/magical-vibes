package com.github.laxika.magicalvibes.model.effect;

/**
 * Each opponent may search their library for a basic land card, put it onto the battlefield tapped,
 * then shuffle. Opponents search in APNAP order (active player first among opponents).
 * The search is optional ("may").
 *
 * <p>Used by Old-Growth Dryads.
 */
public record EachOpponentMaySearchLibraryForBasicLandToBattlefieldTappedEffect() implements CardEffect {
}
