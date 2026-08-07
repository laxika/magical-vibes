package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: "Your maximum hand size is N" (Recycle — two).
 *
 * <p>A "set" value overrides the base/reduced value; when several hand-size effects apply they
 * resolve in timestamp order (CR 402.2), and any "no maximum hand size" effect still wins
 * (handled in {@code TurnCleanupService.hasNoMaximumHandSize}). Checked during the cleanup
 * discard step.
 */
public record SetControllerMaximumHandSizeEffect(int maximumHandSize) implements ControllerMaxHandSizeEffect {

    @Override
    public int applyToMaximumHandSize(int currentMax) {
        return maximumHandSize;
    }
}
