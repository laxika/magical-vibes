package com.github.laxika.magicalvibes.model.filter;

import com.github.laxika.magicalvibes.model.CardSubtype;

/** Matches permanents whose power is at most the number of permanents of the given subtype on the battlefield. */
public record PermanentPowerAtMostSubtypeCountPredicate(CardSubtype subtype)
        implements PermanentPredicate {
}
