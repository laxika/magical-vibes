package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches an artifact if it has the greatest mana value among all artifacts on the battlefield.
 * Multiple artifacts can match if tied for greatest mana value. Requires game data to evaluate.
 */
public record PermanentHasGreatestManaValueAmongAllArtifactsPredicate() implements PermanentPredicate {
}
