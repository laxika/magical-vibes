package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * The controller of this spell/ability takes {@code count} extra turn(s) after this one.
 * When {@code skipUntapStep} is true, each granted extra turn skips its untap step (Savor the Moment).
 * When {@code damageCantBePrevented} is true, damage can't be prevented during each granted turn
 * (Alchemist's Gambit).
 * Power-up abilities are prohibited when {@code powerUpAbilitiesDisabled} is true.
 */
public record ControllerExtraTurnEffect(DynamicAmount count, boolean skipUntapStep,
                                        boolean damageCantBePrevented, boolean powerUpAbilitiesDisabled) implements CardEffect {

    public ControllerExtraTurnEffect(DynamicAmount count, boolean skipUntapStep, boolean damageCantBePrevented) {
        this(count, skipUntapStep, damageCantBePrevented, false);
    }

    public ControllerExtraTurnEffect(int count, boolean skipUntapStep, boolean damageCantBePrevented, boolean powerUpAbilitiesDisabled) {
        this(new Fixed(count), skipUntapStep, damageCantBePrevented, powerUpAbilitiesDisabled);
    }

    public ControllerExtraTurnEffect(int count) {
        this(new Fixed(count), false, false);
    }

    public ControllerExtraTurnEffect(int count, boolean skipUntapStep) {
        this(new Fixed(count), skipUntapStep, false);
    }

    public ControllerExtraTurnEffect(int count, boolean skipUntapStep, boolean damageCantBePrevented) {
        this(new Fixed(count), skipUntapStep, damageCantBePrevented);
    }

    public ControllerExtraTurnEffect(DynamicAmount count) {
        this(count, false, false);
    }

    public ControllerExtraTurnEffect(DynamicAmount count, boolean skipUntapStep) {
        this(count, skipUntapStep, false);
    }
}
