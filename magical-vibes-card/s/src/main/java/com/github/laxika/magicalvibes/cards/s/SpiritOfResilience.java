package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PutCounterThenMayBecomeCopyOfCardsEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SOC", collectorNumber = "37")
@CardRegistration(set = "SOC", collectorNumber = "85")
public class SpiritOfResilience extends Card {

    public SpiritOfResilience() {
        addTrigger(new TriggerCondition(Triggers.leavesGraveyard(null, ObjectRelation.YOU_OWN)),
                TriggerOptions.ONE_OR_MORE,
                new PutCounterThenMayBecomeCopyOfCardsEffect());
    }
}
