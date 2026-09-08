package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.PlusOnePlusOneCounterPutOnControlledPermanentThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;

@CardRegistration(set = "KLD", collectorNumber = "155")
public class FairgroundsTrumpeter extends Card {

    public FairgroundsTrumpeter() {
        addEffect(EffectSlot.END_STEP_TRIGGERED, new ConditionalEffect(
                new PlusOnePlusOneCounterPutOnControlledPermanentThisTurn(),
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE)));
    }
}
