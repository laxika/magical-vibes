package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches spells whose target occurrences all identify the same permanent or player.
 * Repeated occurrences of that permanent or player are allowed.
 */
public record StackEntryTargetsOnlySinglePermanentOrPlayerPredicate() implements StackEntryPredicate {
}
