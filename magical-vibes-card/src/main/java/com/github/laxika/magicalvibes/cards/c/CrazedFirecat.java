package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.FlipUntilLosePutCountersOnSourceEffect;

@CardRegistration(set = "TOR", collectorNumber = "94")
public class CrazedFirecat extends Card {

    public CrazedFirecat() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new FlipUntilLosePutCountersOnSourceEffect());
    }
}
