package com.github.laxika.magicalvibes.model.effect;

import java.util.List;

/**
 * Performs a mandatory cost-like action as an effect instruction. If it cannot be
 * performed, resolves the fallback effects.
 */
public record ForcedCostOrElseEffect(CostEffect forcedCost, List<CardEffect> elseEffects) implements CardEffect {
}
