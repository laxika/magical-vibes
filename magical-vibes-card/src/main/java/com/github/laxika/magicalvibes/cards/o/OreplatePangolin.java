package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;

@CardRegistration(set = "EOE", collectorNumber = "150")
public class OreplatePangolin extends Card {

    public OreplatePangolin() {
        addEffect(EffectSlot.ON_ALLY_ARTIFACT_ENTERS_BATTLEFIELD,
                new MayPayManaEffect("{1}",
                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE),
                        "Pay {1} to put a +1/+1 counter on Oreplate Pangolin?"));
    }
}
