package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Grants the controller permission to play up to {@code count} additional lands this turn,
 * on top of the normal one-per-turn allowance. Used by Summer Bloom.
 */
public record PlayAdditionalLandsEffect(DynamicAmount count) implements CardEffect {

    public PlayAdditionalLandsEffect(int count) {
        this(new Fixed(count));
    }
}
