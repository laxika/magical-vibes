package com.github.laxika.magicalvibes.model.effect;

/**
 * Compound effect: return the source permanent to its owner's hand, then create creature tokens.
 * Used by Thopter Assembly and similar cards that bounce themselves and produce tokens as a single
 * triggered ability resolution.
 */
public record ReturnSelfToHandAndCreateTokensEffect(CreateCreatureTokenEffect tokenEffect) implements CardEffect {
}
