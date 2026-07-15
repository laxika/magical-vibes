package com.github.laxika.magicalvibes.model.effect;

/**
 * The sacrificing player sacrifices a creature; the effect's controller gains life equal to that
 * creature's toughness.
 *
 * <p>When {@code sacrificerIsController} is {@code false} the sacrificing player is a chosen target
 * (an edict — Tribute to Hunger). When {@code true} the controller both sacrifices and gains the
 * life, so the effect does not target (Doomgape's upkeep trigger).
 */
public record SacrificeCreatureAndControllerGainsLifeEqualToToughnessEffect(boolean sacrificerIsController)
        implements CardEffect {
    @Override public TargetSpec targetSpec() {
        return !sacrificerIsController ? TargetSpec.benign(TargetCategory.PLAYER) : TargetSpec.NONE;
    }
}
