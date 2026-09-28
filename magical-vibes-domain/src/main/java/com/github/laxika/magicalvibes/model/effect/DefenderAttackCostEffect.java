package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Permanent;

/**
 * Capability for a static effect that adds a generic mana cost to a creature attacking this
 * effect's controller. The amount may depend on the attacking creature.
 */
public interface DefenderAttackCostEffect extends CardEffect {

    /**
     * Generic mana required for {@code attacker} to attack this effect's controller.
     */
    int attackCost(Permanent attacker);

    /**
     * Whether this cost also applies when the attacker is aimed at a planeswalker controlled by
     * this effect's controller.
     */
    default boolean protectsPlaneswalkers() {
        return true;
    }
}
