package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/** Each opponent may sacrifice a nontoken creature; an opponent who does not loses life. */
public record EachOpponentMaySacrificeNontokenCreatureOrLoseLifeEffect(DynamicAmount lifeLoss)
        implements CardEffect {

    public EachOpponentMaySacrificeNontokenCreatureOrLoseLifeEffect(int lifeLoss) {
        this(new Fixed(lifeLoss));
    }
}
