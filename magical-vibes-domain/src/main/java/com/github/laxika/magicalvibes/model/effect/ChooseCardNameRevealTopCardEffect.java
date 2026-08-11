package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Controller names a card, then reveals the top card of their library. If it has the chosen name,
 * the matching reward is inserted into the resolving ability.
 */
public record ChooseCardNameRevealTopCardEffect() implements CardDrawingEffect {

    @Override
    public DynamicAmount drawnCardAmount() {
        return new Fixed(3);
    }
}
