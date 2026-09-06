package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CreaturesPutIntoOwnGraveyardThisTurn;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;

@CardRegistration(set = "MIR", collectorNumber = "256")
public class AsmiraHolyAvenger extends Card {

    public AsmiraHolyAvenger() {
        // At the beginning of each end step, put a +1/+1 counter on Asmira for each creature
        // put into your graveyard from the battlefield this turn.
        addEffect(EffectSlot.END_STEP_TRIGGERED, new PutCountersOnSelfEffect(
                CounterType.PLUS_ONE_PLUS_ONE, new CreaturesPutIntoOwnGraveyardThisTurn()));
    }
}
