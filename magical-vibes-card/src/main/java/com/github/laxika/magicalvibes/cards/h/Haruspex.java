package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveXCountersFromSourceCost;

import java.util.List;

@CardRegistration(set = "40K", collectorNumber = "91")
public class Haruspex extends Card {

    public Haruspex() {
        addEffect(EffectSlot.ON_ANY_CREATURE_DIES,
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new RemoveXCountersFromSourceCost(CounterType.PLUS_ONE_PLUS_ONE),
                        new AwardAnyColorManaEffect(new XValue())
                ),
                "{T}, Remove X +1/+1 counters from Haruspex: Add X mana of any one color."
        ));
    }
}
