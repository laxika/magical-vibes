package com.github.laxika.magicalvibes.model.effect;

/**
 * Modifies the generic mana portion of flashback costs for the selected players.
 * Positive amounts increase the cost; negative amounts reduce it.
 */
public record ModifyFlashbackCostEffect(int amount, CostModificationScope scope) implements CardEffect {
}
