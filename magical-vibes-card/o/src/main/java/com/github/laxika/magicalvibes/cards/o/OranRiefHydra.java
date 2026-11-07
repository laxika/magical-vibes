package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.condition.TriggeringPermanentHasSubtype;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BFZ", collectorNumber = "181")
@CardRegistration(set = "DDR", collectorNumber = "16")
@CardRegistration(set = "E01", collectorNumber = "69")
public class OranRiefHydra extends Card {

    public OranRiefHydra() {
        // Landfall — Whenever a land you control enters, put a +1/+1 counter on this creature.
        // If that land is a Forest, put two +1/+1 counters on this creature instead. The land is read as the
        // ability resolves, as it last existed on the battlefield once it has left (CR 608.2h).
        addTrigger(Triggers.enters(Triggers.LAND, ObjectRelation.YOU_CONTROL),
                new ConditionalReplacementEffect(new TriggeringPermanentHasSubtype(CardSubtype.FOREST),
                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE, 1),
                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE, 2)));
    }
}
