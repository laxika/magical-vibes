package com.github.laxika.magicalvibes.model.effect;

/**
 * Capability for static effects that reduce damage dealt to creatures controlled by the effect's
 * controller.
 */
public interface ControlledCreaturesDamageReductionEffect extends CardEffect {

    /** Returns the amount removed from each damage event. */
    int amount();
}
