package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DistributeCountersAmongTargetsEffect;

@CardRegistration(set = "MRD", collectorNumber = "140")
public class WurmskinForger extends Card {

    public WurmskinForger() {
        // When this creature enters, distribute three +1/+1 counters among one, two, or three target creatures.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                DistributeCountersAmongTargetsEffect.chosenAmongTargetCreaturesEtb(
                        CounterType.PLUS_ONE_PLUS_ONE, 3));
    }
}
