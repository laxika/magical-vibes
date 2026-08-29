package com.github.laxika.magicalvibes.model.effect;

/**
 * Capability for static effects that prevent a fixed amount of damage from each source to a
 * permanent's controller and creatures that player controls.
 */
public interface ControllerAndCreaturesDamagePreventionEffect extends CardEffect {

    /** Returns the amount prevented from each damage event. */
    int amount();
}
