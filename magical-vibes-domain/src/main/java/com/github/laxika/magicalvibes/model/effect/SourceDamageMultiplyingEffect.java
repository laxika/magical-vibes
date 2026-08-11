package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Static effect that multiplies damage dealt by matching sources controlled by the same player as
 * the permanent carrying this effect.
 */
public interface SourceDamageMultiplyingEffect extends CardEffect {

    int damageMultiplier();

    PermanentPredicate sourceFilter();

    default boolean matchesStackEntrySource(StackEntry entry, Permanent effectSource) {
        return false;
    }
}
