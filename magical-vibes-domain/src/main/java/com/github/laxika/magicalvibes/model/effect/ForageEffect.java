package com.github.laxika.magicalvibes.model.effect;

/** Resolves a forage action and then resolves the follow-up when the forage succeeds. */
public record ForageEffect(CardEffect thenEffect) implements CardEffect {

    /** Creates a forage action with no follow-up effect. */
    public ForageEffect() {
        this(null);
    }
}
