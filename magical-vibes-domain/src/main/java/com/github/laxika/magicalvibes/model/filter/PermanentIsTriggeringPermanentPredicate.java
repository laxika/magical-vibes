package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches the permanent whose event caused the resolving ability to trigger.
 * Requires the trigger's permanent ID in the {@link FilterContext}.
 */
public record PermanentIsTriggeringPermanentPredicate() implements PermanentPredicate {
}
