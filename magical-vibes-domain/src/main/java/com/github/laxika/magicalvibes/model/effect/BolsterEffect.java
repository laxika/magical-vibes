package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Bolsters the controller: at resolution, they choose one of their creatures with the least
 * toughness and put the dynamically evaluated {@code amount} of +1/+1 counters on it.
 */
public record BolsterEffect(DynamicAmount amount, boolean recordChoice) implements CardEffect {

    public BolsterEffect(DynamicAmount amount) {
        this(amount, false);
    }

    public BolsterEffect(int amount) {
        this(new Fixed(amount), false);
    }

    public BolsterEffect(int amount, boolean recordChoice) {
        this(new Fixed(amount), recordChoice);
    }
}
