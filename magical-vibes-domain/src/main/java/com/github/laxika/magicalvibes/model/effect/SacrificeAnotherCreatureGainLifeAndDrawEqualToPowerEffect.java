package com.github.laxika.magicalvibes.model.effect;

/**
 * "Sacrifice another creature. You gain X life and draw X cards, where X is that creature's power."
 *
 * <p>The sacrifice is mandatory when the controller has another creature; with no other creature
 * nothing happens at all. X is the sacrificed creature's effective power as it last existed on the
 * battlefield, clamped at 0 (CR 510.1a-style negative-power clamp), so a 0-power creature gains no
 * life and draws no cards. Does not target — the creature is chosen at resolution
 * (Disciple of Bolas).
 */
public record SacrificeAnotherCreatureGainLifeAndDrawEqualToPowerEffect() implements CardEffect {
}
