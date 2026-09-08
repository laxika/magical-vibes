package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles the source permanent and creates the supplied token only when the source was still on the
 * battlefield and could be exiled.
 *
 * @param token token blueprint created after the source is exiled
 */
public record ExileSelfAndCreateTokenEffect(CreateTokenEffect token) implements CardEffect {
}
