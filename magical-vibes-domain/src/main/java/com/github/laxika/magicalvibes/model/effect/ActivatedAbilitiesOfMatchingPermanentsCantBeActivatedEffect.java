package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Static effect that prevents all activated abilities (including mana abilities) of permanents
 * matching the given predicate from being activated.
 * <p>
 * Used by cards like Stony Silence ("Activated abilities of artifacts can't be activated.").
 */
public record ActivatedAbilitiesOfMatchingPermanentsCantBeActivatedEffect(
        PermanentPredicate predicate
) implements CardEffect {
}
