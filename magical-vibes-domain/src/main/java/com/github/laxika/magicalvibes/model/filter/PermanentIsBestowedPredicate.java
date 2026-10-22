package com.github.laxika.magicalvibes.model.filter;

/** Matches a permanent that is currently a bestowed Aura (CR 702.103f), as opposed to a creature or an ordinary Aura. */
public record PermanentIsBestowedPredicate() implements PermanentPredicate {
}
