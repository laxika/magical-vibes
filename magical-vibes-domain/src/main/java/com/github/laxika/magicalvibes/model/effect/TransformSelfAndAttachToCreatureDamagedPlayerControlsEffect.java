package com.github.laxika.magicalvibes.model.effect;

/**
 * "Transform this permanent. If you do, attach it to target creature that player controls."
 * Used inside a {@link MayEffect} wrapper for combat damage triggers where "that player" is the
 * damaged player. StackEntry context: targetId = damaged player, sourcePermanentId = source permanent.
 */
public record TransformSelfAndAttachToCreatureDamagedPlayerControlsEffect() implements CardEffect {
}
