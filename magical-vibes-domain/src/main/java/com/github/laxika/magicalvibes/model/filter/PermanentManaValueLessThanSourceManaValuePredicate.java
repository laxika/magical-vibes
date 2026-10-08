package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches permanents whose mana value is strictly less than the mana value of the permanent that
 * imposed the filter, or of the entering permanent when {@code useTriggeringPermanent} is true.
 */
public record PermanentManaValueLessThanSourceManaValuePredicate(boolean useTriggeringPermanent) implements PermanentPredicate {
    public PermanentManaValueLessThanSourceManaValuePredicate() {
        this(false);
    }
}
