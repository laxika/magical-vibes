package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TMP", collectorNumber = "153")
@CardRegistration(set = "BRB", collectorNumber = "61")
public class SadisticGlee extends Card {

    public SadisticGlee() {
        target(TargetFilters.creature())
                // Whenever a creature dies, put a +1/+1 counter on enchanted creature.
                .addTrigger(Triggers.creatureDies(ObjectRelation.ANY),
                        new PutCounterOnTargetPermanentEffect(Subjects.attached(), CounterType.PLUS_ONE_PLUS_ONE));
    }
}
