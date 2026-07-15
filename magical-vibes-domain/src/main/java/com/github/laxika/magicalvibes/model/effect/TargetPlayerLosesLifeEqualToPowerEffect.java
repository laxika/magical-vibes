package com.github.laxika.magicalvibes.model.effect;

/**
 * Marker effect for "target player loses life equal to this creature's power".
 * At death-trigger time, {@code DeathTriggerService} reads the dying permanent's
 * effective power and replaces this with a concrete {@link LoseLifeEffect} targeting a player.
 */
public record TargetPlayerLosesLifeEqualToPowerEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetCategory.PLAYER);
    }
}
