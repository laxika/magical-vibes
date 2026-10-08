package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Reduces the activation cost of activated abilities of cards matching {@code filter} in the
 * controller's graveyard by {@code amount} generic mana (static, controller-scoped). E.g. Embalmer's
 * Tools with a creature-card predicate and amount 1.
 */
public record ReduceGraveyardCardActivatedAbilityCostEffect(CardPredicate filter, int amount,
                                                           int minimumManaCost)
        implements GraveyardActivatedAbilityCostReducingEffect {

    public ReduceGraveyardCardActivatedAbilityCostEffect(CardPredicate filter, int amount) {
        this(filter, amount, 0);
    }

    @Override
    public CardPredicate affectedGraveyardCards() {
        return filter;
    }

    @Override
    public int genericCostReduction() {
        return amount;
    }
}
