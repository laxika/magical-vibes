package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreaturesForCostReductionEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TormentOfHailfireEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "BLB", collectorNumber = "107")
public class RottenmouthViper extends Card {

    public RottenmouthViper() {
        addEffect(EffectSlot.STATIC, new SacrificeCreaturesForCostReductionEffect(
                1, new PermanentNotPredicate(new PermanentIsLandPredicate())));

        SequenceEffect blightTrigger = SequenceEffect.of(
                new PutCountersOnSelfEffect(CounterType.BLIGHT),
                new TormentOfHailfireEffect(4, null, new CountersOnSource(CounterType.BLIGHT)));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, blightTrigger);
        addEffect(EffectSlot.ON_ATTACK, blightTrigger);
    }
}
