package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;

import java.util.Set;

public record PreventAllDamageFromChosenSourceEffect(
        boolean controllerOnly,
        Set<CardColor> colorFilter
) implements CardEffect {

    /** Prevents all damage the chosen source would deal to the controller this turn (Auriok Replica). */
    public PreventAllDamageFromChosenSourceEffect() {
        this(true, Set.of());
    }
}
