package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetForEachLeavingSourceCounterEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SPM", collectorNumber = "12")
@CardRegistration(set = "OM1", collectorNumber = "8")
@CardRegistration(set = "MSC", collectorNumber = "774")
public class SelflessPoliceCaptain extends Card {

    public SelflessPoliceCaptain() {
        addStaticEffect(new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new Fixed(1)));

        target(TargetFilters.creatureYouControl()).addTrigger(Triggers.leavesBattlefield(ObjectRelation.THIS),
                new PutCounterOnTargetForEachLeavingSourceCounterEffect(CounterType.PLUS_ONE_PLUS_ONE));
    }
}
