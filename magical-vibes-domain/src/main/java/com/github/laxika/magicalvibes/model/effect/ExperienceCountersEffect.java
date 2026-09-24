package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/** Changes the resolving controller's experience-counter total. */
public record ExperienceCountersEffect(DynamicAmount amount) implements CardEffect {

    public ExperienceCountersEffect(int amount) {
        this(new Fixed(amount));
    }
}
