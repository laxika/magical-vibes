package com.github.laxika.magicalvibes.model.effect;

/**
 * Copies the spell that caused this resolving cast trigger for its controller, then lets that
 * player choose an opponent who also gets a copy.
 */
public record DemonstrateEffect() implements TriggeringSpellReferencingEffect {
}
