package com.github.laxika.magicalvibes.model.effect;

/**
 * Reduces the generic mana cost of the foretell special action for the source permanent's
 * controller and can allow that action during any player's turn.
 */
public record ForetellCostReductionEffect(int amount, boolean allowDuringAnyTurn) implements CardEffect {

    public ForetellCostReductionEffect(int amount) {
        this(amount, false);
    }
}
