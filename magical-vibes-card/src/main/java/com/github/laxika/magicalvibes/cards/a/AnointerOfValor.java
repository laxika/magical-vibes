package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;

@CardRegistration(set = "2X2", collectorNumber = "6")
public class AnointerOfValor extends Card {

    public AnointerOfValor() {
        addEffect(EffectSlot.ON_ANY_CREATURE_ATTACKS, new MayPayManaEffect(
                "{3}",
                new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE),
                "Pay {3} to put a +1/+1 counter on the attacking creature?"));
    }
}
