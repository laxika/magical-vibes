package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * A player gains {@code amount} life. By default the spell/ability's controller gains it; a
 * {@code recipient} of {@link GainLifeRecipient#TARGET_CONTROLLER} instead routes the life to the
 * controller of the effect's target permanent (e.g. Condemn's "its controller gains life equal to
 * its toughness").
 */
public record GainLifeEffect(DynamicAmount amount, GainLifeRecipient recipient) implements CardEffect {

    public GainLifeEffect(DynamicAmount amount) {
        this(amount, GainLifeRecipient.CONTROLLER);
    }

    public GainLifeEffect(int amount) {
        this(new Fixed(amount));
    }
}
