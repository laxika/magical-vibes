package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MillControllerEffect;

@CardRegistration(set = "DOM", collectorNumber = "112")
public class WindgraceAcolyte extends Card {

    public WindgraceAcolyte() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MillControllerEffect(3));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new GainLifeEffect(3));
    }
}
