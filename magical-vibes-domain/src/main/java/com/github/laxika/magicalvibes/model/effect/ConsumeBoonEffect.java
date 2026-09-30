package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.BoonTrigger;

/** Consumes one boon created by the resolving source card after its trigger resolves. */
public record ConsumeBoonEffect(BoonTrigger trigger) implements CardEffect {

    public ConsumeBoonEffect {
        if (trigger == null) {
            throw new IllegalArgumentException("A boon trigger is required");
        }
    }
}
