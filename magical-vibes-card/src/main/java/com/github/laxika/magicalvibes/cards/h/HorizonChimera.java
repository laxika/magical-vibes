package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

@CardRegistration(set = "THS", collectorNumber = "194")
public class HorizonChimera extends Card {

    public HorizonChimera() {
        addEffect(EffectSlot.ON_CONTROLLER_DRAWS, new GainLifeEffect(1));
    }
}
