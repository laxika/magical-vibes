package com.github.laxika.magicalvibes.model.effect;

/**
 * A player sacrifices a creature of their choice; a player then gains life equal to that creature's
 * toughness.
 *
 * <p>When {@code sacrificerIsController} is {@code false} the sacrificing player is a chosen target
 * (an edict — Tribute to Hunger, Devour Flesh). When {@code true} the controller sacrifices and the
 * effect does not target (Doomgape's upkeep trigger).
 *
 * <p>{@code sacrificerGainsLife} splits the two printings of the life clause: Tribute to Hunger's
 * "You gain life equal to that creature's toughness" pays the effect's controller ({@code false}),
 * while Devour Flesh's "then gains life equal to that creature's toughness" pays the player who
 * sacrificed ({@code true}).
 */
public record SacrificeCreatureAndControllerGainsLifeEqualToToughnessEffect(
        boolean sacrificerIsController,
        boolean sacrificerGainsLife
) implements CardEffect {
    @Override public TargetSpec targetSpec() {
        return !sacrificerIsController ? TargetSpec.benign(TargetCategory.PLAYER) : TargetSpec.NONE;
    }
}
