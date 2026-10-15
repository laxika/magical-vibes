package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreaturesEnterAsCopyOfSourceEffect;

@CardRegistration(set = "ISD", collectorNumber = "178")
public class EssenceOfTheWild extends Card {

    public EssenceOfTheWild() {
        addEffect(EffectSlot.STATIC, new CreaturesEnterAsCopyOfSourceEffect());
    }
}
