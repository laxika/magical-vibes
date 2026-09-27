package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** Static effect that grants blitz with a card's mana cost to matching creature spells. */
public record GrantBlitzToSpellsEffect(CardPredicate filter, DynamicAmount costReduction)
        implements BlitzGrantingEffect {

    @Override
    public CardPredicate blitzGrantFilter() {
        return filter;
    }

    @Override
    public DynamicAmount blitzCostReduction() {
        return costReduction;
    }
}
