package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MillControllerEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "ISD", collectorNumber = "43")
public class ArmoredSkaab extends Card {

    public ArmoredSkaab() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MillControllerEffect(4));
    }
}
