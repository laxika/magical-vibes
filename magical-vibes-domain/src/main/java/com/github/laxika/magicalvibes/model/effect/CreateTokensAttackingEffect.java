package com.github.laxika.magicalvibes.model.effect;

/** Creates a counted group of tokens that enter tapped and attacking after their attack targets are chosen. */
public record CreateTokensAttackingEffect(int amount, CreateTokenEffect tokenEffect) implements CardEffect {
}
