package com.github.laxika.magicalvibes.model.effect;

import java.util.List;

/**
 * Performs a cost-like action as an effect instruction. If it cannot be performed,
 * resolves the fallback effects.
 *
 * <p>When {@code optional} is true the action is a "you may" choice ("you may sacrifice an
 * artifact. If you don't, ..."): the controller is asked, and declining (or being unable to
 * pay) resolves the fallback effects. When false the action is mandatory and the fallback only
 * fires if the cost cannot be paid at all (e.g. Archdemon of Greed).
 */
public record ForcedCostOrElseEffect(CostEffect forcedCost, List<CardEffect> elseEffects, boolean optional) implements CardEffect {
    public ForcedCostOrElseEffect(CostEffect forcedCost, List<CardEffect> elseEffects) {
        this(forcedCost, elseEffects, false);
    }
}
