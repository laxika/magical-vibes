package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches permanents represented by a physical double-faced card, whether they are currently
 * showing their front or back face. A meld card, or a melded permanent, isn't one (CR 712.1).
 */
public record PermanentIsDoubleFacedPredicate() implements PermanentPredicate {
}
