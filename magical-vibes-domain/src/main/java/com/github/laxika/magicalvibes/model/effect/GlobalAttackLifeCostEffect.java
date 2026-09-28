package com.github.laxika.magicalvibes.model.effect;

/**
 * Capability for a floating attack tax paid in life for each attacking creature.
 */
public interface GlobalAttackLifeCostEffect extends CardEffect {

    /** Life required for each creature declared as an attacker. */
    int lifeCostPerCreature();

    /** Whether the tax also applies to attacks against the defender's planeswalkers. */
    default boolean protectsPlaneswalkers() {
        return true;
    }
}
