package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** Static effect that grants a fixed prowl cost to matching spells the controller casts. */
public record GrantProwlToSpellsEffect(String prowlCost, CardPredicate filter)
        implements ProwlGrantingEffect {

    public GrantProwlToSpellsEffect {
        if (prowlCost == null || prowlCost.isBlank()) {
            throw new IllegalArgumentException("Prowl cost must not be blank");
        }
    }

    @Override
    public CardPredicate prowlGrantFilter() {
        return filter;
    }
}
