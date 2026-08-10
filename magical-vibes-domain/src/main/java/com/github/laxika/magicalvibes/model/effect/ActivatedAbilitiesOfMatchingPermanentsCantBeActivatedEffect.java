package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Static effect that prevents activated abilities of permanents matching the given predicate
 * from being activated.
 */
public record ActivatedAbilitiesOfMatchingPermanentsCantBeActivatedEffect(
        PermanentPredicate predicate,
        boolean blocksManaAbilities
) implements CardEffect {

    public ActivatedAbilitiesOfMatchingPermanentsCantBeActivatedEffect(PermanentPredicate predicate) {
        this(predicate, true);
    }
}
