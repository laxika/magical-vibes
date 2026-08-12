package com.github.laxika.magicalvibes.model.effect;

/**
 * Gives control of the source permanent to the player carried on the stack entry. The normal form
 * targets that player; {@link #triggeringPlayer()} uses a non-targeting player supplied by the
 * surrounding spell-cast trigger.
 */
public record TargetPlayerGainsControlOfSourceCreatureEffect(boolean targetsPlayer) implements CardEffect {

    public TargetPlayerGainsControlOfSourceCreatureEffect() {
        this(true);
    }

    public static TargetPlayerGainsControlOfSourceCreatureEffect triggeringPlayer() {
        return new TargetPlayerGainsControlOfSourceCreatureEffect(false);
    }

    @Override
    public TargetSpec targetSpec() {
        return targetsPlayer ? TargetSpec.benign(TargetPredicates.player()) : TargetSpec.NONE;
    }
}
