package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Used with {@code ON_ANY_PERMANENT_DEALS_DAMAGE_TO_YOU}. Destroys the permanent that dealt
 * damage if it matches the supplied predicate.
 */
public record DestroyDamageSourcePermanentEffect(PermanentPredicate filter) implements CardEffect {
}
