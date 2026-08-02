package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * Cost that removes X counters of the given type from the source permanent, where X is the
 * activation's chosen X — "Remove X theft counters from this enchantment: ..." (Night Dealings).
 *
 * <p>Unlike an {@code {X}} mana cost, the X here is bounded by the counters actually on the source,
 * so the client prompts for it the way it prompts for a variable loyalty cost. The chosen X rides
 * along on the activation as its {@code xValue}, so an ability effect can read it back with an
 * {@code XValue} amount.
 */
public record RemoveXCountersFromSourceCost(CounterType counterType) implements CostEffect {
}
