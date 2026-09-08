package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Increases this spell's own casting cost by the given amount of generic mana.
 * The effect is read from the spell's {@link com.github.laxika.magicalvibes.model.EffectSlot#STATIC}
 * effects while the spell is being cast.
 */
public record IncreaseOwnCastCostEffect(DynamicAmount amount) implements CardEffect {

    public IncreaseOwnCastCostEffect(int amount) {
        this(new Fixed(amount));
    }
}
