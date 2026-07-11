package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Controller draws {@code amount} cards, one at a time (so draw-replacement effects and
 * "whenever you draw" triggers see each individual draw).
 */
public record DrawCardEffect(DynamicAmount amount) implements CardEffect {

    public DrawCardEffect() {
        this(1);
    }

    public DrawCardEffect(int amount) {
        this(new Fixed(amount));
    }
}
