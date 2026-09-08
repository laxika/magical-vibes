package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeXPermanentsCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;

@CardRegistration(set = "PCY", collectorNumber = "137")
public class CopperLeafAngel extends Card {

    public CopperLeafAngel() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new SacrificeXPermanentsCost(new PermanentIsLandPredicate()),
                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE, new XValue())
                ),
                "{T}, Sacrifice X lands: Put X +1/+1 counters on this creature."
        ));
    }
}
