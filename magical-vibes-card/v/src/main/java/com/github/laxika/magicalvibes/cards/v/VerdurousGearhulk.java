package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.DistributeCountersAmongTargetsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "KLD", collectorNumber = "172")
@CardRegistration(set = "MPS", collectorNumber = "5")
@CardRegistration(set = "KLR", collectorNumber = "185")
@CardRegistration(set = "MIC", collectorNumber = "145")
public class VerdurousGearhulk extends Card {

    public VerdurousGearhulk() {
        targetUpTo(new Fixed(4), TargetFilters.creatureYouControl(), 4)
                .addTrigger(Triggers.enters(ObjectRelation.THIS),
                        DistributeCountersAmongTargetsEffect.chosenAmongTargetCreaturesEtb(
                                CounterType.PLUS_ONE_PLUS_ONE, 4,
                                new PermanentControlledBySourceControllerPredicate()));
    }
}
