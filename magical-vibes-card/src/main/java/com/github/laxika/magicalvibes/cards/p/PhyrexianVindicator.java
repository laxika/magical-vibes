package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreventDamageToSelfAndDealThatMuchDamageEffect;

@CardRegistration(set = "ONE", collectorNumber = "27")
public class PhyrexianVindicator extends Card {

    public PhyrexianVindicator() {
        addEffect(EffectSlot.STATIC, new PreventDamageToSelfAndDealThatMuchDamageEffect());
    }
}
