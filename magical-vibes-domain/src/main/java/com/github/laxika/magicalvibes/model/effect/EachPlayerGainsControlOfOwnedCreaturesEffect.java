package com.github.laxika.magicalvibes.model.effect;

/**
 * Each player gains permanent control of every creature they own.
 *
 * <p>This is non-targeting and applies to creatures across all battlefields, including creatures
 * currently controlled by another player.
 */
public record EachPlayerGainsControlOfOwnedCreaturesEffect() implements ControlStealingEffect {

    @Override
    public ControlDuration controlDuration() {
        return ControlDuration.PERMANENT;
    }
}
