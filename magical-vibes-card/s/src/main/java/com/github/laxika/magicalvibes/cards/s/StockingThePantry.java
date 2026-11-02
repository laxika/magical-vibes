package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "BLB", collectorNumber = "194")
public class StockingThePantry extends Card {

    public StockingThePantry() {
        addTrigger(Triggers.youPutPlusOneCountersOnCreatureYouControl(),
                new PutCountersOnSelfEffect(CounterType.SUPPLY));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(
                        new RemoveCounterFromSourceCost(1, CounterType.SUPPLY),
                        new DrawCardEffect(1)
                ),
                "{2}, Remove a supply counter from this enchantment: Draw a card."
        ));
    }
}
