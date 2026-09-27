package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** Static effect that grants either a fixed blitz cost or a reduced mana-cost blitz. */
public record GrantBlitzToSpellsEffect(String blitzCost, CardPredicate filter, DynamicAmount costReduction)
        implements BlitzGrantingEffect {

    public GrantBlitzToSpellsEffect(String blitzCost, CardPredicate filter) {
        this(blitzCost, filter, null);
        if (blitzCost == null || blitzCost.isBlank()) {
            throw new IllegalArgumentException("Blitz cost must not be blank");
        }
    }

    public GrantBlitzToSpellsEffect(CardPredicate filter, DynamicAmount costReduction) {
        this(null, filter, costReduction);
    }

    @Override
    public CardPredicate blitzGrantFilter() {
        return filter;
    }

    @Override
    public DynamicAmount blitzCostReduction() {
        return costReduction;
    }
}
