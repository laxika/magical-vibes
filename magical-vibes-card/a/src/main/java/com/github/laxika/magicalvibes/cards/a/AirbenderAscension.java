package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.AirbendTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.FlickerEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TLA", collectorNumber = "6")
public class AirbenderAscension extends Card {

    public AirbenderAscension() {
        target(TargetFilters.creature(), 0, 1)
                .addTrigger(Triggers.enters(ObjectRelation.THIS), new AirbendTargetPermanentEffect());

        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL),
                new PutCountersOnSelfEffect(CounterType.QUEST));

        target(TargetFilters.creatureYouControl(), 0, 1)
                .addTrigger(Triggers.endStep(ActivePlayer.YOU),
                        TriggerOptions.DEFAULT.withInterveningIf(new SourceCounterThreshold(4, CounterType.QUEST)),
                        FlickerEffect.flickerTarget());
    }
}
