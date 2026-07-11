package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches a card whose printed power is less than or equal to {@code maxPower}. Cards without a
 * power (non-creatures) never match. Used for library searches such as "a creature card with
 * power 2 or less" (Imperial Recruiter).
 */
public record CardPowerAtMostPredicate(int maxPower) implements CardPredicate {
}
