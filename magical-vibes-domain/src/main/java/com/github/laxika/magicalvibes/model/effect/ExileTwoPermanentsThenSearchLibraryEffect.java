package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/** Has the controller choose and exile two matching permanents, then search their library. */
public record ExileTwoPermanentsThenSearchLibraryEffect(
        PermanentPredicate firstFilter,
        String firstLabel,
        PermanentPredicate secondFilter,
        String secondLabel,
        CardPredicate searchFilter
) implements CardEffect {
}
