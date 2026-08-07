package com.github.laxika.magicalvibes.model.effect;

/**
 * Static marker: creatures the controller's opponents control with hexproof can be the targets of
 * spells and abilities the controller controls as though they didn't have hexproof (Glaring
 * Spotlight). Scanned at targeting time by the target-legality services (never resolved on the
 * stack) — it lifts the hexproof gate only, leaving shroud and protection intact.
 */
public record IgnoreOpponentCreatureHexproofEffect() implements CardEffect {
}
