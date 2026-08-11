package com.github.laxika.magicalvibes.model.effect;

/**
 * Skips the controller's draw step in the current turn. Repeated applications in the same turn
 * collapse to one skip, matching effects that refer to "your draw step this turn".
 */
public record SkipDrawStepThisTurnEffect() implements CardEffect {
}
