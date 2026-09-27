package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** Static effect that grants a fixed blitz cost to matching spells cast from hand. */
public record GrantBlitzToSpellsEffect(String blitzCost, CardPredicate filter)
        implements BlitzGrantingEffect {

    public GrantBlitzToSpellsEffect {
        if (blitzCost == null || blitzCost.isBlank()) {
            throw new IllegalArgumentException("Blitz cost must not be blank");
        }
    }

    @Override
    public CardPredicate blitzGrantFilter() {
        return filter;
    }
}
