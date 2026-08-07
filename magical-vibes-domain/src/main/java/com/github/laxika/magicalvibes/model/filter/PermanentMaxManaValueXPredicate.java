package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches permanents whose card mana value is less than or equal to X, where X comes from the
 * {@link FilterContext#xValue()} at evaluation time. Used for spells like "Return all nonland
 * permanents with mana value X or less to their owners' hands" (Displacement Wave).
 */
public record PermanentMaxManaValueXPredicate() implements PermanentPredicate {
}
