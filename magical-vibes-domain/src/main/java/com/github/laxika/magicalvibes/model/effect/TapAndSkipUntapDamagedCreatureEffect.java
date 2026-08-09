package com.github.laxika.magicalvibes.model.effect;

/**
 * Marker for a self-scoped damage-to-creature trigger that taps the damaged creature and makes it
 * skip its controller's next untap step. The trigger collector expands it into the two ordinary
 * permanent effects, so this marker is never resolved directly.
 */
public record TapAndSkipUntapDamagedCreatureEffect() implements CardEffect {
}
