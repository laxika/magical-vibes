package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches permanents whose card mana value is at most X, where X comes from the
 * {@link FilterContext#xValue()} at evaluation time. Used by Displacement Wave and Quillmane Baku;
 * the {@code or less} sibling of {@link PermanentManaValueEqualsXPredicate}.
 */
public record PermanentMaxManaValueXPredicate() implements PermanentPredicate {
}
