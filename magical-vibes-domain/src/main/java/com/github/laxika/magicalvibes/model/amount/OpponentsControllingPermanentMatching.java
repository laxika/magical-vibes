package com.github.laxika.magicalvibes.model.amount;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/** The number of opponents of the source controller who control at least one permanent matching {@code filter}. */
public record OpponentsControllingPermanentMatching(PermanentPredicate filter) implements DynamicAmount {
}
