package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FIN", collectorNumber = "26")
@CardRegistration(set = "FIN", collectorNumber = "430")
public class MinwuWhiteMage extends Card {

    public MinwuWhiteMage() {
        // Whenever you gain life, put a +1/+1 counter on each Cleric you control.
        addTrigger(Triggers.youGainLife(), new PutCounterOnEachControlledPermanentEffect(
                CounterType.PLUS_ONE_PLUS_ONE,
                1,
                new PermanentHasSubtypePredicate(CardSubtype.CLERIC)));
    }
}
