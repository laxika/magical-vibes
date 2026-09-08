package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SetControllerMaximumHandSizeToSourceCountersEffect;

@CardRegistration(set = "KLD", collectorNumber = "92")
public class MidnightOil extends Card {

    public MidnightOil() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.HOUR, new Fixed(7)));
        addEffect(EffectSlot.STATIC,
                new SetControllerMaximumHandSizeToSourceCountersEffect(CounterType.HOUR));
        addEffect(EffectSlot.DRAW_TRIGGERED, SequenceEffect.of(
                new DrawCardEffect(1),
                new RemoveCounterFromSourceEffect(CounterType.HOUR, 2)));
        addEffect(EffectSlot.ON_CONTROLLER_DISCARDS, new LoseLifeEffect(1));
    }
}
