package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ManifestTopCardAndPutCountersEffect;

@CardRegistration(set = "FRF", collectorNumber = "98")
public class FierceInvocation extends Card {

    public FierceInvocation() {
        addEffect(EffectSlot.SPELL,
                new ManifestTopCardAndPutCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, 2));
    }
}
