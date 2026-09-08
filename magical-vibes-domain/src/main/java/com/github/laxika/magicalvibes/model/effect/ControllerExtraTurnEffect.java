package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * The controller of this spell/ability takes {@code count} extra turn(s) after this one.
 * When {@code skipUntapStep} is true, each granted extra turn skips its untap step (Savor the Moment).
 * When {@code powerUpAbilitiesDisabled} is true, Power-up abilities can't be activated during each
 * granted extra turn.
 */
public record ControllerExtraTurnEffect(DynamicAmount count, boolean skipUntapStep,
                                        boolean powerUpAbilitiesDisabled) implements CardEffect {

    public ControllerExtraTurnEffect(int count) {
        this(new Fixed(count), false, false);
    }

    public ControllerExtraTurnEffect(int count, boolean skipUntapStep) {
        this(new Fixed(count), skipUntapStep, false);
    }

    public ControllerExtraTurnEffect(int count, boolean skipUntapStep, boolean powerUpAbilitiesDisabled) {
        this(new Fixed(count), skipUntapStep, powerUpAbilitiesDisabled);
    }

    public ControllerExtraTurnEffect(DynamicAmount count) {
        this(count, false);
    }

    public ControllerExtraTurnEffect(DynamicAmount count, boolean skipUntapStep) {
        this(count, skipUntapStep, false);
    }
}
