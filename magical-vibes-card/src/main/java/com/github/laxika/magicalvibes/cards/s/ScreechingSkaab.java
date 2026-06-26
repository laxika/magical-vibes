package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MillControllerEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "DKA", collectorNumber = "47")
public class ScreechingSkaab extends Card {

    public ScreechingSkaab() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MillControllerEffect(2));
    }
}
