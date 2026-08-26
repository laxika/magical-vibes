package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AddOnePlusOneCountersToSourceEffect;

@CardRegistration(set = "WAR", collectorNumber = "167")
public class MowuLoyalCompanion extends Card {

    public MowuLoyalCompanion() {
        addEffect(EffectSlot.STATIC, new AddOnePlusOneCountersToSourceEffect());
    }
}
