package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches a card whose owner is not the player evaluating the predicate (i.e. "a spell you don't
 * own"). Evaluated with the perspective player supplied as the {@code cardOwnerId} argument of
 * {@code PredicateEvaluationService.matchesCardPredicate}; in the spell-cast trigger path that
 * argument is the casting player. Cards with no tracked owner (tokens/copies) never match.
 *
 * <p>Used by Nita, Forum Conciliator's "Whenever you cast a spell you don't own" trigger.
 */
public record CardControllerDoesNotOwnPredicate() implements CardPredicate {
}
