package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Target player mills {@code count} cards. The amount is a {@link DynamicAmount}, so the
 * same effect covers a fixed count ("mills three cards"), an X value ({@code XValue}), or a
 * derived count such as charge counters on the source ({@code CountersOnSource(CHARGE)}).
 */
public record MillTargetPlayerEffect(DynamicAmount count) implements CardEffect {

    /** Convenience constructor for a fixed mill count. */
    public MillTargetPlayerEffect(int count) {
        this(new Fixed(count));
    }

    @Override
    public boolean canTargetPlayer() {
        return true;
    }
}
