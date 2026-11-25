package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;

@CardRegistration(set = "MSH", collectorNumber = "163")
public class ClaimTheKingdom extends Card {

    public ClaimTheKingdom() {
        target(TargetFilters.creatureYouControl())
                .addTrigger(Triggers.enters(Triggers.LAND, ObjectRelation.YOU_CONTROL), SequenceEffect.of(
                        new PutCountersEffect(CounterType.PLUS_ONE_PLUS_ONE),
                        PutCountersEffect.onSelf(CounterType.PLAN)));

        target(TargetFilters.creatureYouControl())
                .addTrigger(Triggers.countersPutOnThis(), TriggerOptions.DEFAULT.withEventCondition(
                        new SourceCounterThreshold(4, CounterType.PLAN)),
                        SequenceEffect.of(new SacrificeSelfEffect(),
                                new ConditionalEffect(HappenedThisWay.sacrificed(), new QueueReflexiveAbilityEffect(new PutCountersEffect(CounterType.INDESTRUCTIBLE)))));
    }
}
