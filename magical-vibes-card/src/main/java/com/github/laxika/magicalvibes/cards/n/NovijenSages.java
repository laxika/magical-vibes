package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.MoveCounterFromSourceToEnteringCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromControlledCreatureCost;

import java.util.List;

@CardRegistration(set = "DIS", collectorNumber = "27")
public class NovijenSages extends Card {

    public NovijenSages() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new Fixed(4)));
        addEffect(EffectSlot.ON_ANY_OTHER_CREATURE_ENTERS_BATTLEFIELD,
                new MoveCounterFromSourceToEnteringCreatureEffect(CounterType.PLUS_ONE_PLUS_ONE));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(
                        new RemoveCounterFromControlledCreatureCost(2, CounterType.PLUS_ONE_PLUS_ONE),
                        new DrawCardEffect(1)
                ),
                "{1}, Remove two +1/+1 counters from among creatures you control: Draw a card."
        ));
    }
}
