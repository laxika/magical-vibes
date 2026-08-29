package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.StackEntryPredicate;

/**
 * Capability for static effects that multiply damage dealt by sources controlled by the effect
 * permanent's controller.
 */
public interface ControllerDamageMultiplyingEffect extends CardEffect {

    int damageMultiplier();

    StackEntryPredicate stackFilter();

    boolean appliesToCombatDamage();
}
