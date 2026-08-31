package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "BLB", collectorNumber = "229")
public class PondProphet extends Card {

    public PondProphet() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DrawCardEffect(1));
    }
}
