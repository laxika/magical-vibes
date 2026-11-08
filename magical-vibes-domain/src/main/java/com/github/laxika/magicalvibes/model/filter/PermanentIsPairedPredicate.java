package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches a permanent that is soulbond-paired with another creature (CR 702.95). Soulbond's "if … both are unpaired"
 * reads it on the creature that entered, through {@code TriggeringPermanentMatches(new PermanentNotPredicate(…))}.
 */
public record PermanentIsPairedPredicate() implements PermanentPredicate {
}
