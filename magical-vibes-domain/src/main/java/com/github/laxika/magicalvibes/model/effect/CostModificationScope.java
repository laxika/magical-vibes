package com.github.laxika.magicalvibes.model.effect;

/**
 * Determines whose spells are affected by a cost modification effect.
 */
public enum CostModificationScope {

    /**
     * Affects spells cast by the controller of the permanent carrying this effect.
     */
    SELF,

    /**
     * Affects spells cast by opponents of the permanent's controller.
     */
    OPPONENT
}
