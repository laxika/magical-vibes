package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches cards that have a cycling ability (a hand-activated ability whose description begins with
 * "Cycling", the engine's convention for cycling — see {@code AbilityActivationService}).
 */
public record CardHasCyclingPredicate() implements CardPredicate {
}
