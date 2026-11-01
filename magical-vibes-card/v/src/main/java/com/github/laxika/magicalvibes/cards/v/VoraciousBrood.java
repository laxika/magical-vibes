package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MSC", collectorNumber = "735")
public class VoraciousBrood extends Card {

    public VoraciousBrood() {
        addStaticEffect(new EnterWithCountersEffect(
                        CounterType.PLUS_ONE_PLUS_ONE,
                        new CardsInGraveyard(new CardTypePredicate(CardType.CREATURE), CountScope.CONTROLLER)));

        addTrigger(Triggers.putIntoGraveyardFromAnywhereBut(Zone.LIBRARY, Triggers.NONTOKEN_CREATURE,
                ObjectRelation.ANOTHER_YOU_OWN),
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE));
        addTrigger(new TriggerCondition(Triggers.putIntoGraveyardFrom(Zone.LIBRARY, Triggers.NONTOKEN_CREATURE,
                ObjectRelation.YOU_OWN)),
                TriggerOptions.ONE_OR_MORE,
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE, new EventValue()));
    }
}
