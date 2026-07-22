package com.github.laxika.magicalvibes.model.effect;

/**
 * Transforms the permanent referenced by the stack entry's {@code targetId} (to its other face).
 * Used when "it" is fixed at trigger time — e.g. Vildin-Pack Alpha's "you may transform it" for
 * the Werewolf that just entered — not a targeting choice. Non-transformable permanents (no back
 * face) are a no-op; transform-prevention is respected.
 */
public record TransformTargetPermanentEffect() implements CardEffect {
}
