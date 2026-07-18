package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches a card that has the same name as any permanent on any battlefield (Mitotic
 * Manipulation's "a card with the same name as a permanent"). Requires the {@code GameData}
 * overload of {@code PredicateEvaluationService.matchesCardPredicate}; without game state it
 * matches nothing.
 */
public record CardSharesNameWithAPermanentPredicate() implements CardPredicate {
}
