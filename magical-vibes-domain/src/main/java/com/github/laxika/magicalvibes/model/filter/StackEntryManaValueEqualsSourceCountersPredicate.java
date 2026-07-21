package com.github.laxika.magicalvibes.model.filter;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * Matches a spell on the stack whose mana value (including chosen X) equals the number of
 * {@code counterType} counters on the evaluating source permanent.
 * <p>
 * Used for "whenever you cast a spell with mana value equal to the number of [type] counters
 * on this" (e.g. Imminent Doom). Source-dependent: evaluated via
 * {@code TargetLegalityService.matchesStackEntryPredicate(..., source)}.
 */
public record StackEntryManaValueEqualsSourceCountersPredicate(CounterType counterType)
        implements StackEntryPredicate {
}
