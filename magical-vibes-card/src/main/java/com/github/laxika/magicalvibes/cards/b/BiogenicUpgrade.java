package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.DistributeCountersAmongTargetsEffect;
import com.github.laxika.magicalvibes.model.effect.DoubleCountersOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "RNA", collectorNumber = "123")
public class BiogenicUpgrade extends Card {

    public BiogenicUpgrade() {
        // Distribute three +1/+1 counters among one, two, or three target creatures, then double
        // the number of +1/+1 counters on each of those creatures.
        target(TargetFilters.creature(), 1, 3)
                .addEffect(EffectSlot.SPELL, DistributeCountersAmongTargetsEffect.chosenAmongTargetCreatures(
                        CounterType.PLUS_ONE_PLUS_ONE, new Fixed(3)))
                .addEffect(EffectSlot.SPELL,
                        new DoubleCountersOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE));
    }
}
