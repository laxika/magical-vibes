package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.CreatureDeathsThisTurn;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;

@CardRegistration(set = "ME1", collectorNumber = "75")
public class KhabLGhoul extends Card {

    public KhabLGhoul() {
        addEffect(EffectSlot.END_STEP_TRIGGERED, new PutCountersOnSelfEffect(
                CounterType.PLUS_ONE_PLUS_ONE, new CreatureDeathsThisTurn(CountScope.ANY_PLAYER)));
    }
}
