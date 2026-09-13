package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnAttackingCreatureOnAttacksYouEffect;

@CardRegistration(set = "IMA", collectorNumber = "159")
public class CurseOfPredation extends Card {

    public CurseOfPredation() {
        addEffect(EffectSlot.ON_ANY_CREATURE_ATTACKS,
                new PutCounterOnAttackingCreatureOnAttacksYouEffect(CounterType.PLUS_ONE_PLUS_ONE));
    }
}
