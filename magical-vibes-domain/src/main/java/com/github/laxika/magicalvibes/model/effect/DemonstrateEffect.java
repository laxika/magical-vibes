package com.github.laxika.magicalvibes.model.effect;

/**
 * Copies the spell that caused this resolving cast trigger for its controller, then lets that
 * player choose an opponent who also gets a copy. The spell is the stack entry's triggering spell, as it was when the
 * ability triggered.
 */
public record DemonstrateEffect() implements CardEffect {}
