package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PromiseOfLoyaltyEffect;

@CardRegistration(set = "SOC", collectorNumber = "161")
public class PromiseOfLoyalty extends Card {

    public PromiseOfLoyalty() {
        addEffect(EffectSlot.SPELL, new PromiseOfLoyaltyEffect());
    }
}
