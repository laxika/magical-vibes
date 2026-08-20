package com.github.laxika.magicalvibes.model.effect;

/** Creates a counted group of tokens that enter tapped and attacking after their attack targets are chosen. */
public record CreateTokensAttackingEffect(int amount, CreateTokenEffect tokenEffect, boolean sacrificeAtEndStep)
        implements CardEffect {

    public CreateTokensAttackingEffect(int amount, CreateTokenEffect tokenEffect) {
        this(amount, tokenEffect, false);
    }
}
