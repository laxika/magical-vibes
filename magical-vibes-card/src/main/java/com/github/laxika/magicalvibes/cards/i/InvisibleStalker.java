package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;

@CardRegistration(set = "ISD", collectorNumber = "60")
public class InvisibleStalker extends Card {

    public InvisibleStalker() {
        addEffect(EffectSlot.STATIC, new CantBeBlockedEffect());
    }
}
