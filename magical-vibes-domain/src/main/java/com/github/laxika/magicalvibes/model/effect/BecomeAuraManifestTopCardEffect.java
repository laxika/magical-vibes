package com.github.laxika.magicalvibes.model.effect;

/**
 * Lightform-style enter-the-battlefield effect: the source becomes an Aura with enchant creature,
 * manifests the top card of its controller's library, and attaches to that face-down creature.
 */
public record BecomeAuraManifestTopCardEffect() implements CardEffect {
}
