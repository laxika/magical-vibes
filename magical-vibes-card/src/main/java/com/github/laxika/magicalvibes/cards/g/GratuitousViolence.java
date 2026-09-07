package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DoubleDamageFromCreaturesEffect;

@CardRegistration(set = "ONS", collectorNumber = "212")
public class GratuitousViolence extends Card {

    public GratuitousViolence() {
        addEffect(EffectSlot.STATIC, new DoubleDamageFromCreaturesEffect());
    }
}
