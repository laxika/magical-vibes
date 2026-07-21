package com.github.laxika.magicalvibes.model.effect;

/**
 * When resolved, registers a delayed trigger so the source permanent transforms to its back face
 * at the beginning of the next upkeep. Used by Archangel Avacyn: "When a non-Angel creature you
 * control dies, transform Archangel Avacyn at the beginning of the next upkeep."
 *
 * <p>Drained in {@code StepTriggerService.handleUpkeepTriggers}; the delayed ability resolves as
 * {@link TransformToBackFaceEffect} (no-op if already transformed or the permanent is gone).
 */
public record RegisterTransformSourceAtNextUpkeepEffect() implements CardEffect {
}
