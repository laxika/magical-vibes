package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.condition.Condition;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Used with {@code ON_ANY_PERMANENT_DEALS_DAMAGE_TO_YOU}. Exiles the permanent that dealt the damage
 * until the watching permanent leaves the battlefield (O-ring linkage), then returns it under its
 * owner's control. Used by Hixus, Prison Warden.
 *
 * @param filter        restriction on the damage source (e.g. creatures only), or {@code null} for any
 * @param combatOnly    only fire on combat damage
 * @param intervening   optional intervening-"if" condition (CR 603.4), checked both when the ability
 *                      would trigger and again on resolution, or {@code null} for none
 */
public record ExileDamageSourcePermanentUntilSourceLeavesEffect(PermanentPredicate filter,
                                                                boolean combatOnly,
                                                                Condition intervening)
        implements CardEffect {

    public ExileDamageSourcePermanentUntilSourceLeavesEffect(PermanentPredicate filter, boolean combatOnly) {
        this(filter, combatOnly, null);
    }
}
