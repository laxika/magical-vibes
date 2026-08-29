package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * The controller of this spell/ability takes {@code count} extra turn(s) after this one.
 * When {@code skipUntapStep} is true, each granted extra turn skips its untap step (Savor the Moment).
 */
public record ControllerExtraTurnEffect(DynamicAmount count, boolean skipUntapStep) implements CardEffect {

    public ControllerExtraTurnEffect(int count) {
        this(new Fixed(count), false);
    }

    public ControllerExtraTurnEffect(int count, boolean skipUntapStep) {
        this(new Fixed(count), skipUntapStep);
    }

    public ControllerExtraTurnEffect(DynamicAmount count) {
        this(count, false);
    }
}
