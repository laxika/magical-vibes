package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.Morbid;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;

@CardRegistration(set = "HOB", collectorNumber = "66")
@CardRegistration(set = "HOB", collectorNumber = "206")
public class DreadedBatCloud extends Card {

    public DreadedBatCloud() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new Morbid(), new ReduceOwnCastCostEffect(new Fixed(3))));
    }
}
