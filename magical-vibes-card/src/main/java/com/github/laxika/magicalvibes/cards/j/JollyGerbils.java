package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "BLB", collectorNumber = "19")
public class JollyGerbils extends Card {

    public JollyGerbils() {
        addEffect(EffectSlot.ON_CONTROLLER_GIVES_GIFT, new DrawCardEffect());
    }
}
