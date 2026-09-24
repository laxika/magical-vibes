package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** Static effect that grants a fixed freerunning cost to matching spells. */
public record GrantFreerunningToSpellsEffect(String freerunningCost, CardPredicate filter)
        implements FreerunningGrantingEffect {

    public GrantFreerunningToSpellsEffect {
        if (freerunningCost == null || freerunningCost.isBlank()) {
            throw new IllegalArgumentException("Freerunning cost must not be blank");
        }
    }

    @Override
    public CardPredicate freerunningGrantFilter() {
        return filter;
    }
}
