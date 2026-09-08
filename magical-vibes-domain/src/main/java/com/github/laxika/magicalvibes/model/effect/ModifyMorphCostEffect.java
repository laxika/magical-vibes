package com.github.laxika.magicalvibes.model.effect;

/**
 * Modifies the generic mana portion of morph costs paid to turn a face-down permanent face up.
 * Positive amounts increase the cost; negative amounts reduce it.
 */
public record ModifyMorphCostEffect(int amount, CostModificationScope scope) implements CardEffect {
}
