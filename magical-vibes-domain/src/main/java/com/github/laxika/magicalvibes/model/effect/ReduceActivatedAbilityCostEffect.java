package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Reduces the generic mana portion of activated abilities of permanents matching {@code predicate}
 * by {@code amount}, for all players (static, symmetric). E.g. Heartstone with a creature
 * predicate and amount 1.
 */
public record ReduceActivatedAbilityCostEffect(PermanentPredicate predicate, int amount)
        implements ActivatedAbilityCostReducingEffect {

    @Override
    public PermanentPredicate affectedPermanents() {
        return predicate;
    }

    @Override
    public int genericCostReduction() {
        return amount;
    }
}
