package com.github.laxika.magicalvibes.model.effect;

/**
 * Capability for a floating effect that adds generic mana to each creature's attack cost,
 * regardless of which player or permanent the creature attacks.
 */
public interface GlobalAttackCostEffect extends CardEffect {

    /**
     * Generic mana required for each creature declared as an attacker.
     */
    int attackCostPerCreature();
}
