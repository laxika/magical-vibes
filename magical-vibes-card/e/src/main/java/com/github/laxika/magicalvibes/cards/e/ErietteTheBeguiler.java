package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainControlOfAuraAttachedPermanentEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "OTJ", collectorNumber = "202")
public class ErietteTheBeguiler extends Card {

    public ErietteTheBeguiler() {
        addTrigger(Triggers.auraYouControlBecomesAttachedToNonlandPermanentOpponentControls(),
                new GainControlOfAuraAttachedPermanentEffect());
    }
}
