package com.github.laxika.magicalvibes.model.effect;

/**
 * "Sacrifice this creature. If you do, destroy target creature that player controls."
 * Used inside a MayEffect wrapper for combat damage / unblocked-attack triggers where "that player"
 * is the damaged / defending player.
 * Context: StackEntry.targetId = affected player ID, StackEntry.sourcePermanentId = source creature ID.
 * {@code cannotBeRegenerated} = the destroyed creature can't be regenerated (e.g. Necrite).
 */
public record SacrificeSelfToDestroyCreatureDamagedPlayerControlsEffect(boolean cannotBeRegenerated) implements CardEffect {

    public SacrificeSelfToDestroyCreatureDamagedPlayerControlsEffect() {
        this(false);
    }
}
