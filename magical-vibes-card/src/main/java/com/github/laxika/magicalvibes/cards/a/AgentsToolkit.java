package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.MoveChosenCounterFromSourceToEnteringCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "NCC", collectorNumber = "66")
@CardRegistration(set = "NCC", collectorNumber = "166")
public class AgentsToolkit extends Card {

    private static final List<CounterType> COUNTER_TYPES = List.of(
            CounterType.PLUS_ONE_PLUS_ONE,
            CounterType.FLYING,
            CounterType.DEATHTOUCH,
            CounterType.SHIELD
    );

    public AgentsToolkit() {
        for (CounterType counterType : COUNTER_TYPES) {
            addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                    new EnterWithCountersEffect(counterType, new Fixed(1)));
        }

        addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new MoveChosenCounterFromSourceToEnteringCreatureEffect(java.util.Arrays.stream(CounterType.values())
                        .filter(type -> type != CounterType.ANY && type != CounterType.SILVER).toList()));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(new SacrificeSelfCost(), new DrawCardEffect()),
                "{2}, Sacrifice this artifact: Draw a card."
        ));
    }
}
