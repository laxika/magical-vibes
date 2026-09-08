package com.github.laxika.magicalvibes.model.effect;

/**
 * Capability for a floating effect that adds generic mana to each creature's block cost.
 */
public interface GlobalBlockCostEffect extends CardEffect {

    /**
     * Generic mana required for each creature declared as a blocker.
     */
    int blockCostPerCreature();
}
