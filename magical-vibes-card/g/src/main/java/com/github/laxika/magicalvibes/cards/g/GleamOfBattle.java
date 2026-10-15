package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;

@CardRegistration(set = "DGM", collectorNumber = "73")
public class GleamOfBattle extends Card {

    public GleamOfBattle() {
        // Whenever a creature you control attacks, put a +1/+1 counter on it. The attacking creature
        // is baked in as the trigger's (non-targeting) target, so "it" is the target permanent.
        addEffect(EffectSlot.ON_ALLY_CREATURE_ATTACKS,
                new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1));
    }
}
