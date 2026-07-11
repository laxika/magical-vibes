package com.github.laxika.magicalvibes.model.effect;

/**
 * Each opponent may search their library for a creature card, put it onto the battlefield,
 * then shuffle. Opponents search in APNAP order (active player first among opponents).
 * The search is optional ("may").
 *
 * <p>Used by Boldwyr Heavyweights.
 */
public record EachOpponentMaySearchLibraryForCreatureToBattlefieldEffect() implements CardEffect {
}
