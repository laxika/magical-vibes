package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.EachPlayerExilesCardFromGraveyardThenEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;

@CardRegistration(set = "SOC", collectorNumber = "11")
@CardRegistration(set = "SOC", collectorNumber = "61")
public class AugustaOrderReturned extends Card {

    public AugustaOrderReturned() {
        // When one or more nonland cards are exiled by the attack trigger, put that many +1/+1
        // counters on target attacking creature.
        var counterEffect = new PutCounterOnTargetPermanentEffect(
                CounterType.PLUS_ONE_PLUS_ONE, new EventValue(), null,
                new PermanentIsAttackingPredicate(), false, null);
        addEffect(EffectSlot.ON_ATTACK,
                new EachPlayerExilesCardFromGraveyardThenEffect(counterEffect));
    }
}
