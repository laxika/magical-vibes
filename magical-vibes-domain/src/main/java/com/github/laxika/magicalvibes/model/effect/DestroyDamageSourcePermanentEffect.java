package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Used with {@code Triggers.permanentDealsDamageToYou()}. Destroys the permanent that dealt
 * damage if it matches the supplied predicate.
 */
public record DestroyDamageSourcePermanentEffect(PermanentPredicate filter) implements CardEffect {
}
