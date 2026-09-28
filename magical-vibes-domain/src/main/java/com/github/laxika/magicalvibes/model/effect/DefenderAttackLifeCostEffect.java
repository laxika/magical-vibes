package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Permanent;

/**
 * Capability for a defender-scoped attack tax paid in life for each attacking creature.
 */
public interface DefenderAttackLifeCostEffect extends CardEffect {

    /** Life required for {@code attacker} to attack this effect's controller. */
    int lifeCost(Permanent attacker);

    /** Whether this cost also applies when attacking a planeswalker controlled by the defender. */
    default boolean protectsPlaneswalkers() {
        return true;
    }
}
