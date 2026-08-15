package com.github.laxika.magicalvibes.model.filter;

import com.github.laxika.magicalvibes.model.CardSubtype;

/**
 * Matches permanents whose effective toughness is at most the number of permanents
 * with the given subtype controlled by the source's controller.
 */
public record PermanentToughnessAtMostControlledSubtypeCountPredicate(CardSubtype subtype)
        implements PermanentPredicate {
}
