package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches a card whose printed power is greater than or equal to {@code minPower}. Cards without a
 * power (non-creatures) never match. Used for "creature cards with power 5 or greater" style
 * clauses (Sacellum Godspeaker).
 */
public record CardPowerAtLeastPredicate(int minPower) implements CardPredicate {
}
