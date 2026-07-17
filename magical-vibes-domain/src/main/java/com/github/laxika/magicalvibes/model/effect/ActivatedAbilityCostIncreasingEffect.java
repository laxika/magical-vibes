package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Capability interface for static effects that tax the activation cost of activated abilities of
 * matching permanents (e.g. Gloom: "Activated abilities of white enchantments cost {3} more to
 * activate."). The tax is symmetric — it applies to every player's matching permanents — and is
 * collected by {@code CastingCostService} without naming the concrete effect type.
 */
public interface ActivatedAbilityCostIncreasingEffect extends CardEffect {

    /** The permanents whose activated abilities are taxed. */
    PermanentPredicate affectedPermanents();

    /** Extra generic mana required to activate a matching permanent's ability. */
    int additionalGenericCost();
}
