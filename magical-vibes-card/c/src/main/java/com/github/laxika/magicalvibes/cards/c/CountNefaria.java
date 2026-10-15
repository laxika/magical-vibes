package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.ControllerSacrificedPermanentThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;

@CardRegistration(set = "MSC", collectorNumber = "651")
public class CountNefaria extends Card {

    public CountNefaria() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new ControllerSacrificedPermanentThisTurn(), new ReduceOwnCastCostEffect(new Fixed(3))));
    }
}
