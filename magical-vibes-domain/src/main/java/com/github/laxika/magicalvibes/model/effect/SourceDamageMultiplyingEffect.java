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

    /** Whether this multiplier also applies when the matching source deals noncombat damage. */
    default boolean appliesToNonCombatDamage() {
        return true;
    }

    /** Whether this multiplier applies to combat damage aimed at the supplied permanent. */
    default boolean appliesToCombatDamageTarget(Permanent target) {
        return true;
    }

    default boolean matchesStackEntrySource(StackEntry entry, Permanent effectSource) {
        return false;
    }
}
