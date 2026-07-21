package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches cards that have a cycling ability (or a typecycling / landcycling variant — a
 * hand-activated ability whose description name ends with {@code "cycling"}; see
 * {@code ActivatedAbility#isCyclingAbility()}).
 */
public record CardHasCyclingPredicate() implements CardPredicate {
}
