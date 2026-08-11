package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;

@CardRegistration(set = "M19", collectorNumber = "64")
@CardRegistration(set = "THS", collectorNumber = "57")
public class Omenspeaker extends Card {

    public Omenspeaker() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ScryEffect(2));
    }
}
