package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AdditionalControllerDamageToOpponentsAndTheirPermanentsEffect;

@CardRegistration(set = "MSC", collectorNumber = "756")
public class ThorAsgardsAvenger extends Card {

    public ThorAsgardsAvenger() {
        addStaticEffect(new AdditionalControllerDamageToOpponentsAndTheirPermanentsEffect(1, false, true));
    }
}
