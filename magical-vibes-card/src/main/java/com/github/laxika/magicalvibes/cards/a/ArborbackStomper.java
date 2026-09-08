package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

@CardRegistration(set = "KLD", collectorNumber = "142")
public class ArborbackStomper extends Card {

    public ArborbackStomper() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new GainLifeEffect(5));
    }
}
