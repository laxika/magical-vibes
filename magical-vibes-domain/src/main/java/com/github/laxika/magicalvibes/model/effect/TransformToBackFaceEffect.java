package com.github.laxika.magicalvibes.model.effect;

/**
 * Transforms the source permanent to its back face if it is still on the battlefield and not
 * already transformed. No-ops otherwise (Archangel Avacyn delayed trigger: multiple deaths queue
 * multiple delayed abilities, but only the first that resolves while still front-face transforms).
 */
public record TransformToBackFaceEffect() implements CardEffect {
}
