package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnEnteringCreatureEffect;

@CardRegistration(set = "FDN", collectorNumber = "240")
public class GoodFortuneUnicorn extends Card {

    public GoodFortuneUnicorn() {
        // Whenever another creature you control enters, put a +1/+1 counter on that creature.
        addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new PutCountersOnEnteringCreatureEffect(1, false));
    }
}
