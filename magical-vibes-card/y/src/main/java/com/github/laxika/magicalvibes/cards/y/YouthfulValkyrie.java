package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "FDN", collectorNumber = "149")
@CardRegistration(set = "FDN", collectorNumber = "303")
@CardRegistration(set = "KHM", collectorNumber = "382")
@CardRegistration(set = "SLD", collectorNumber = "1588")
@CardRegistration(set = "FDC", collectorNumber = "58")
public class YouthfulValkyrie extends Card {

    public YouthfulValkyrie() {
        // Whenever another Angel you control enters, put a +1/+1 counter on this creature.
        addTrigger(Triggers.enters(
                        new PermanentAllOfPredicate(List.of(
                                Triggers.CREATURE,
                                new PermanentHasSubtypePredicate(CardSubtype.ANGEL))),
                        ObjectRelation.ANOTHER_YOU_CONTROL),
                PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE));
    }
}
