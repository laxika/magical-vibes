package com.github.laxika.magicalvibes.model.filter;

/**
 * The permanent enlisted a creature this combat: its controller paid the cost of its enlist ability by tapping another
 * creature as it attacked (CR 702.154c). "Whenever a creature you control attacks, if it enlisted a creature this
 * combat" (Aradesh, the Founder) is {@code Triggers.creatureYouControlAttacks()} with the intervening "if"
 * {@code TriggeringPermanentMatches(new PermanentEnlistedCreatureThisCombatPredicate())}. It stays true for the rest of
 * that combat whatever happens to the enlisted creature, and for a permanent that left the battlefield, judged as it
 * last existed.
 */
public record PermanentEnlistedCreatureThisCombatPredicate() implements PermanentPredicate {
}
