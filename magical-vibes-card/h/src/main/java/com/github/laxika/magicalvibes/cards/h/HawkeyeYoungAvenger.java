package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.AdditionalControllerDamageToOpponentsAndTheirPermanentsEffect;

@CardRegistration(set = "MSH", collectorNumber = "131")
public class HawkeyeYoungAvenger extends Card {

    public HawkeyeYoungAvenger() {
        addEffect(EffectSlot.STATIC,
                new AdditionalControllerDamageToOpponentsAndTheirPermanentsEffect(new SourcePower(), true));
    }
}
