package com.github.laxika.magicalvibes.model.effect;

/**
 * Destroys one creature with the least effective power among all creatures on the battlefield.
 * If multiple creatures are tied, the effect controller chooses one when the effect resolves.
 *
 * @param cannotBeRegenerated whether regeneration shields are ignored
 */
public record DestroyCreatureWithLeastPowerEffect(boolean cannotBeRegenerated) implements RemovalEffect {

    @Override
    public RemovalKind removalKind() {
        return RemovalKind.DESTROY;
    }
}
