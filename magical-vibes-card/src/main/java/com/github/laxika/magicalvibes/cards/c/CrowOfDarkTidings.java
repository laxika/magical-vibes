package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;

@CardRegistration(set = "SOI", collectorNumber = "105")
public class CrowOfDarkTidings extends Card {

    public CrowOfDarkTidings() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MillEffect(2, MillRecipient.CONTROLLER));
        addEffect(EffectSlot.ON_DEATH, new MillEffect(2, MillRecipient.CONTROLLER));
    }
}
