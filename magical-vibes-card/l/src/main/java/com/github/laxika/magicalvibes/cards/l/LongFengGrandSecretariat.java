package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "TLA", collectorNumber = "233")
public class LongFengGrandSecretariat extends Card {

    public LongFengGrandSecretariat() {
        target(TargetFilters.creatureYouControl())
                .addTrigger(Triggers.dies(new PermanentAnyOfPredicate(List.of(Triggers.CREATURE, Triggers.LAND)),
                                ObjectRelation.ANOTHER_YOU_CONTROL),
                        new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1))
                // "Or a land you control": Long Feng itself only when it was a land.
                .addTrigger(Triggers.dies(Triggers.LAND, ObjectRelation.THIS),
                        new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1));
    }
}
