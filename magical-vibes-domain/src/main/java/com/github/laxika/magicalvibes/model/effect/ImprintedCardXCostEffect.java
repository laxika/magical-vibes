package com.github.laxika.magicalvibes.model.effect;

/**
 * Marker for activated-ability effects whose {X} cost is defined by the source permanent's imprinted
 * (exiled) card rather than chosen freely — "X is the mana value of that card" (Prototype Portal,
 * Elite Arcanist). Activation then requires an imprinted card and an X equal to its mana value.
 */
public interface ImprintedCardXCostEffect extends CardEffect {

    /**
     * Whether this instance actually carries the X-equals-imprinted-mana-value restriction. Effects
     * shared with abilities that have no {X} at all (Mimic Vat) return {@code false}.
     */
    default boolean requiresImprintedXCost() {
        return true;
    }
}
