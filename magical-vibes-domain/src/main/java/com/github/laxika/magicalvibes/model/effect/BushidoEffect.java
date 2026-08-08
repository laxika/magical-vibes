package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * A Bushido ability. The combat trigger slots that represent Bushido use this effect so other
 * cards can distinguish Bushido from unrelated block triggers. The amount is a
 * {@link DynamicAmount} so fixed bushido N and variable bushido X share one record.
 */
public record BushidoEffect(DynamicAmount amount) implements CardEffect {

    /** Convenience for printed fixed bushido ("Bushido 2"). */
    public BushidoEffect(int amount) {
        this(new Fixed(amount));
    }

    public BoostSelfEffect asBoost() {
        return new BoostSelfEffect(amount, amount);
    }
}
