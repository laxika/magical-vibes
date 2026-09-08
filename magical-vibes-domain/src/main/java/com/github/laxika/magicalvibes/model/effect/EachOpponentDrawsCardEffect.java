package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/** Each opponent of the resolving controller draws the evaluated number of cards. */
public record EachOpponentDrawsCardEffect(DynamicAmount amount) implements CardEffect {

    public EachOpponentDrawsCardEffect(int amount) {
        this(new Fixed(amount));
    }
}
