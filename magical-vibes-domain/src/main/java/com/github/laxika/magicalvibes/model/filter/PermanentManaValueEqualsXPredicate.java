package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches permanents whose card mana value equals X, where X comes from the
 * {@link FilterContext#xValue()} at evaluation time. Used for spells like
 * "Gain control of target creature with mana value X" (Entrancing Melody).
 */
public record PermanentManaValueEqualsXPredicate() implements PermanentPredicate {
}
