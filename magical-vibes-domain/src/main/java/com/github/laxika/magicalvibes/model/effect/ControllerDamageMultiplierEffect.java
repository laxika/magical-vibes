package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.StackEntryPredicate;

/**
 * Static replacement effect that multiplies damage from sources controlled by this permanent's
 * controller.
 */
public record ControllerDamageMultiplierEffect(
        int multiplier,
        StackEntryPredicate stackFilter,
        boolean appliesToCombatDamage
) implements ControllerDamageMultiplyingEffect {

    @Override
    public int damageMultiplier() {
        return multiplier;
    }
}
