package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Each player (in turn order) draws {@code amount} cards. Use {@code XValue} for "each player
 * draws X" (Prosperity); the fixed-count ctor covers "each player draws N".
 */
public record EachPlayerDrawsCardEffect(DynamicAmount amount) implements CardEffect {

    public EachPlayerDrawsCardEffect(int amount) {
        this(new Fixed(amount));
    }
}
