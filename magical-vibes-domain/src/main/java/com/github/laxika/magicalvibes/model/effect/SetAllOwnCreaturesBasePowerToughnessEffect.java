package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Sets the base power and toughness of every creature the controller controls to the given
 * values until end of turn (CR 613, layer 7b: counters and other modifiers still apply on top).
 * The amounts are {@link DynamicAmount}s so "base power and toughness X/X" (where X is a paid
 * cost, e.g. Mirror Entity) and a fixed value are the same effect. The amount is evaluated once
 * on resolution and applied uniformly to every creature you control.
 */
public record SetAllOwnCreaturesBasePowerToughnessEffect(
        DynamicAmount power,
        DynamicAmount toughness
) implements CardEffect {

    /** Convenience for a plain fixed base P/T. */
    public SetAllOwnCreaturesBasePowerToughnessEffect(int power, int toughness) {
        this(new Fixed(power), new Fixed(toughness));
    }
}
