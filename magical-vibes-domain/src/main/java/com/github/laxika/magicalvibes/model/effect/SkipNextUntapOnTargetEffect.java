package com.github.laxika.magicalvibes.model.effect;

/**
 * Effect: "Target permanent doesn't untap during its controller's next untap step."
 *
 * <p>When resolved, increments {@code skipUntapCount} on the target permanent.
 * During the untap step, the counter prevents untapping and is decremented.</p>
 *
 * <p>This effect does NOT declare {@code canTargetPermanent()} because it piggybacks
 * on the targeting from a companion effect (e.g. {@link TapTargetPermanentEffect}).</p>
 *
 * <p>Used by Frost Titan's ETB/attack trigger.</p>
 */
public record SkipNextUntapOnTargetEffect() implements CardEffect {
}
