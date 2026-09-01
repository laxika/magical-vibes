package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.DrawGameEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceThenEffect;

@CardRegistration(set = "LEG", collectorNumber = "8")
public class DivineIntervention extends Card {

    public DivineIntervention() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.INTERVENTION, new Fixed(2)));
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new RemoveCounterFromSourceThenEffect(
                        CounterType.INTERVENTION, new DrawGameEffect(), true));
    }
}
