package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EnteringCreaturePowerBranchEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;

@CardRegistration(set = "MOM", collectorNumber = "211")
public class TributeToTheWorldTree extends Card {

    public TributeToTheWorldTree() {
        addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new EnteringCreaturePowerBranchEffect(
                        3,
                        new DrawCardEffect(1),
                        new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 2)));
    }
}
