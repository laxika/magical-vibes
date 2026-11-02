package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCounterSum;
import com.github.laxika.magicalvibes.model.effect.DoubleCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "NEC", collectorNumber = "24")
@CardRegistration(set = "NEC", collectorNumber = "64")
public class AscendantAcolyte extends Card {

    public AscendantAcolyte() {
        addStaticEffect(new EnterWithCountersEffect(
                CounterType.PLUS_ONE_PLUS_ONE,
                new PermanentCounterSum(
                        CounterType.PLUS_ONE_PLUS_ONE,
                        new PermanentIsCreaturePredicate(),
                        CountScope.CONTROLLER)));
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                new DoubleCountersOnSourceEffect(CounterType.PLUS_ONE_PLUS_ONE));
    }
}
