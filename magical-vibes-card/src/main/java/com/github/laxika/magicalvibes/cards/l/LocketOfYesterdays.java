package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForSameNameCardsInGraveyardEffect;

@CardRegistration(set = "TSP", collectorNumber = "258")
public class LocketOfYesterdays extends Card {

    public LocketOfYesterdays() {
        addEffect(EffectSlot.STATIC, new ReduceCastCostForSameNameCardsInGraveyardEffect());
    }
}
