package com.github.laxika.magicalvibes.model.filter;

import com.github.laxika.magicalvibes.model.CardSubtype;

/** Matches creatures whose power is at most the number of permanents of the given subtype the source controller controls. */
public record PermanentPowerAtMostControlledSubtypeCountPredicate(CardSubtype subtype)
        implements PermanentPredicate {
}
