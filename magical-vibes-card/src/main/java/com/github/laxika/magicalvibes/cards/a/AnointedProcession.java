package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MultiplyTokenCreationEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "AKH", collectorNumber = "2")
public class AnointedProcession extends Card {

    public AnointedProcession() {
        addEffect(EffectSlot.STATIC, new MultiplyTokenCreationEffect(2));
    }
}
