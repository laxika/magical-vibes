package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches a spell whose mana value, including chosen X, is greater than the evaluating
 * controller's experience-counter total.
 */
public record StackEntryManaValueGreaterThanControllerExperienceCountersPredicate()
        implements StackEntryPredicate {
}
