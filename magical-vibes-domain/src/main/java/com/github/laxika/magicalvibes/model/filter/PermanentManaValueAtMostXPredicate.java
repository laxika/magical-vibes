package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches permanents whose card mana value is at most X, where X comes from the
 * {@link FilterContext#xValue()} at evaluation time. Used for effects like
 * "Destroy each nonland permanent with mana value X or less" (Gaze of Granite).
 */
public record PermanentManaValueAtMostXPredicate() implements PermanentPredicate {
}
