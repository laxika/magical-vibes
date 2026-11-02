package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.condition.CollectEvidenceCostPaid;
import com.github.laxika.magicalvibes.model.effect.CollectEvidenceCost;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MKM", collectorNumber = "49")
public class CrimestopperSprite extends Card {

    public CrimestopperSprite() {
        addCastingCost(new CollectEvidenceCost(6, true));
        target(TargetFilters.creature())
                .addTrigger(Triggers.enters(ObjectRelation.THIS), SequenceEffect.of(
                        new TapPermanentsEffect(TapUntapScope.TARGET),
                        new ConditionalEffect(
                                new CollectEvidenceCostPaid(),
                                new PutCounterOnTargetPermanentEffect(CounterType.STUN))));
    }
}
