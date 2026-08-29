package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DoubleControllerDamageToOpponentsAndTheirPermanentsEffect;

@CardRegistration(set = "FDN", collectorNumber = "97")
public class TwinflameTyrant extends Card {

    public TwinflameTyrant() {
        addEffect(EffectSlot.STATIC, new DoubleControllerDamageToOpponentsAndTheirPermanentsEffect());
    }
}
