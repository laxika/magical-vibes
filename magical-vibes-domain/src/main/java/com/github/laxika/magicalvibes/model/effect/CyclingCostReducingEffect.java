package com.github.laxika.magicalvibes.model.effect;

/**
 * Capability for static effects that reduce the generic mana portion of cycling abilities.
 */
public interface CyclingCostReducingEffect extends CardEffect {

    /** Generic mana removed from a cycling ability's activation cost. */
    int genericCostReduction();
}
