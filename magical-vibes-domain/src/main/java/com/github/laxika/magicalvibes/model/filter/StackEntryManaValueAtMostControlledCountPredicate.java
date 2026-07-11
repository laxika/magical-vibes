package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches a spell on the stack whose mana value is at most the number of permanents the
 * targeting player controls that satisfy {@code countFilter}.
 * <p>
 * Used for "counter target spell with mana value X or less, where X is the number of [type]
 * you control" (e.g. Spellstutter Sprite counts Faeries). The count is evaluated in the
 * targeting context, so it naturally includes the source permanent itself when it matches.
 */
public record StackEntryManaValueAtMostControlledCountPredicate(PermanentPredicate countFilter)
        implements StackEntryPredicate {
}
