package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

@CardRegistration(set = "M20", collectorNumber = "306")
@CardRegistration(set = "RNA", collectorNumber = "12")
@CardRegistration(set = "ANB", collectorNumber = "10")
@CardRegistration(set = "PIP", collectorNumber = "162")
@CardRegistration(set = "PIP", collectorNumber = "690")
public class ImpassionedOrator extends Card {

    public ImpassionedOrator() {
        addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD, new GainLifeEffect(1));
    }
}
