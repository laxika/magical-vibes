package com.github.laxika.magicalvibes.model.effect;

/**
 * Activation cost that exiles a spell controlled by the activating player from the stack.
 * The no-argument form retains the original instant-or-sorcery-only behavior.
 */
public record ExileInstantOrSorcerySpellCost(boolean anySpell) implements CostEffect {

    public ExileInstantOrSorcerySpellCost() {
        this(false);
    }

    public static ExileInstantOrSorcerySpellCost anySpellCost() {
        return new ExileInstantOrSorcerySpellCost(true);
    }
}
