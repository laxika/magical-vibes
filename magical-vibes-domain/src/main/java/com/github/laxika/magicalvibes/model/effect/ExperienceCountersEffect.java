package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/** Adds experience counters to the controller of the resolving effect. */
public record ExperienceCountersEffect(DynamicAmount amount) implements CardEffect {

    public ExperienceCountersEffect {
        if (amount == null) {
            throw new NullPointerException("amount");
        }
        if (amount instanceof Fixed fixed && fixed.value() <= 0) {
            throw new IllegalArgumentException("Experience counter amount must be positive");
        }
    }

    public ExperienceCountersEffect(int amount) {
        this(new Fixed(amount));
    }
}
