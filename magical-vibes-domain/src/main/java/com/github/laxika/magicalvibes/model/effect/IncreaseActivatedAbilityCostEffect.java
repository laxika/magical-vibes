package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Increases the activation cost of activated abilities of permanents matching {@code predicate}
 * by {@code amount} generic mana, for all players (static, symmetric). E.g. Gloom with a
 * white-enchantment predicate and amount 3.
 */
public record IncreaseActivatedAbilityCostEffect(PermanentPredicate predicate, int amount)
        implements ActivatedAbilityCostIncreasingEffect {

    @Override
    public PermanentPredicate affectedPermanents() {
        return predicate;
    }

    @Override
    public int additionalGenericCost() {
        return amount;
    }
}
