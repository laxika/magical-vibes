package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.FixedIfCondition;
import com.github.laxika.magicalvibes.model.condition.TriggeringPermanentHasSubtype;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;

@CardRegistration(set = "BFZ", collectorNumber = "181")
@CardRegistration(set = "DDR", collectorNumber = "16")
public class OranRiefHydra extends Card {

    public OranRiefHydra() {
        // Landfall — Whenever a land you control enters, put a +1/+1 counter on this creature.
        // If that land is a Forest, put two +1/+1 counters on this creature instead.
        addEffect(EffectSlot.ON_ALLY_LAND_ENTERS_BATTLEFIELD,
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE,
                        new FixedIfCondition(
                                new TriggeringPermanentHasSubtype(CardSubtype.FOREST), 2, 1)));
    }
}
