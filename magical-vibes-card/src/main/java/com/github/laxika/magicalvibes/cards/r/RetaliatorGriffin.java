package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;

@CardRegistration(set = "ARB", collectorNumber = "123")
public class RetaliatorGriffin extends Card {

    public RetaliatorGriffin() {
        // Whenever a source an opponent controls deals damage to you, you may put that many
        // +1/+1 counters on this creature. (Flying is loaded from Scryfall.)
        addEffect(EffectSlot.ON_CONTROLLER_DEALT_DAMAGE_BY_OPPONENT,
                new MayEffect(
                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE, new EventValue()),
                        "Put that many +1/+1 counters on Retaliator Griffin?"));
    }
}
