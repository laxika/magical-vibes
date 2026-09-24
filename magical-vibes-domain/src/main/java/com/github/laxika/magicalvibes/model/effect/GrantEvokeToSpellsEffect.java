package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** Static effect that grants a fixed evoke cost to matching permanent spells the controller casts. */
public record GrantEvokeToSpellsEffect(String evokeCost, CardPredicate filter)
        implements EvokeGrantingEffect {

    public GrantEvokeToSpellsEffect {
        if (evokeCost == null || evokeCost.isBlank()) {
            throw new IllegalArgumentException("Evoke cost must not be blank");
        }
    }

    @Override
    public CardPredicate evokeGrantFilter() {
        return filter;
    }
}
