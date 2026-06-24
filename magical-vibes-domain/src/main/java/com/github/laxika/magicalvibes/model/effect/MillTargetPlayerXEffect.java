package com.github.laxika.magicalvibes.model.effect;

/**
 * Target player mills X cards, where X is the stack entry's chosen X value.
 *
 * @param castWithFlashbackMultiplier multiplier to apply when the spell was cast with flashback
 */
public record MillTargetPlayerXEffect(int castWithFlashbackMultiplier) implements CardEffect {

    public MillTargetPlayerXEffect() {
        this(1);
    }

    @Override
    public boolean canTargetPlayer() {
        return true;
    }
}
