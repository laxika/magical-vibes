package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.FlipCoinWinEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceEffect;

@CardRegistration(set = "TMP", collectorNumber = "168")
public class ChaoticGoo extends Card {

    public ChaoticGoo() {
        // "This creature enters with three +1/+1 counters on it."
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new Fixed(3)));

        // "At the beginning of your upkeep, you may flip a coin. If you win the flip, put a +1/+1
        //  counter on this creature. If you lose the flip, remove a +1/+1 counter from this creature."
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new MayEffect(
                        new FlipCoinWinEffect(
                                new PutCountersOnSourceEffect(1, 1, 1),
                                new RemoveCounterFromSourceEffect(CounterType.PLUS_ONE_PLUS_ONE, 1)),
                        "Flip a coin for Chaotic Goo?"));
    }
}
