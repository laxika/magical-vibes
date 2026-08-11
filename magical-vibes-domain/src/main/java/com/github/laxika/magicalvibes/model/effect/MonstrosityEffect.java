package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/** Resolves a permanent's monstrosity ability, putting {@code amount} +1/+1 counters on it once. */
public record MonstrosityEffect(DynamicAmount amount) implements CardEffect {

    public MonstrosityEffect(int amount) {
        this(new Fixed(amount));
    }
}
