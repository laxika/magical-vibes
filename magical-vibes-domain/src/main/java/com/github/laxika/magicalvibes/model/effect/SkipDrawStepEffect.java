package com.github.laxika.magicalvibes.model.effect;

/**
 * Static marker effect: "Skip your draw step." While the controller controls a permanent
 * with this effect, their draw step is skipped (no turn-based draw, no draw-step triggers).
 */
public record SkipDrawStepEffect() implements CardEffect {
}
