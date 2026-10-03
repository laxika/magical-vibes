package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost;

import java.util.List;

@CardRegistration(set = "EOC", collectorNumber = "133")
public class CrystallineCrawler extends Card {

    public CrystallineCrawler() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new XValue()));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new RemoveCounterFromSourceCost(1, CounterType.PLUS_ONE_PLUS_ONE),
                        new AwardAnyColorManaEffect()
                ),
                "Remove a +1/+1 counter from this creature: Add one mana of any color."
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new PutCountersOnSourceEffect(1, 1, 1)),
                "{T}: Put a +1/+1 counter on this creature."
        ));
    }
}
