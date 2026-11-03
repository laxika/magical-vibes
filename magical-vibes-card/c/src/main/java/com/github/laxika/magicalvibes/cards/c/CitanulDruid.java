package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ATQ", collectorNumber = "31")
@CardRegistration(set = "ME4", collectorNumber = "146")
public class CitanulDruid extends Card {

    public CitanulDruid() {
        addTrigger(Triggers.opponentCastsSpell().withFilter(new CardTypePredicate(CardType.ARTIFACT)),
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE));
    }
}
