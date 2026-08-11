package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ActivatedAbility;

/**
 * Capability interface for static effects that change when matching activated abilities may be
 * activated.
 */
public interface ActivatedAbilityTimingEffect extends CardEffect {

    /** Returns whether this effect makes the given ability available at instant speed. */
    boolean allowsInstantSpeedActivation(ActivatedAbility ability);
}
