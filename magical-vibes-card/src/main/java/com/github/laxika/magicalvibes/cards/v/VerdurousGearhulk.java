package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.DistributeCountersAmongTargetsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "KLD", collectorNumber = "172")
public class VerdurousGearhulk extends Card {

    public VerdurousGearhulk() {
        targetUpTo(new Fixed(4), TargetFilters.creatureYouControl(), 4)
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        DistributeCountersAmongTargetsEffect.chosenAmongTargetCreaturesEtb(
                                CounterType.PLUS_ONE_PLUS_ONE, 4,
                                new PermanentControlledBySourceControllerPredicate()));
    }
}
