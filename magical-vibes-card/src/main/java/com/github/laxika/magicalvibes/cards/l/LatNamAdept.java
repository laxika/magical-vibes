package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;

@CardRegistration(set = "BRO", collectorNumber = "56")
public class LatNamAdept extends Card {

    public LatNamAdept() {
        addEffect(EffectSlot.ON_CONTROLLER_DRAWS, PutCountersOnSourceEffect.onSecondControllerDraw());
    }
}
