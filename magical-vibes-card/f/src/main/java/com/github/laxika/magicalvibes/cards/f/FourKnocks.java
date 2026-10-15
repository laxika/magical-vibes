package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterAndSacrificeSelfOnLastEffect;

@CardRegistration(set = "WHO", collectorNumber = "20")
@CardRegistration(set = "WHO", collectorNumber = "340")
@CardRegistration(set = "WHO", collectorNumber = "625")
@CardRegistration(set = "WHO", collectorNumber = "931")
public class FourKnocks extends Card {

    public FourKnocks() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.TIME, new Fixed(4)));
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new RemoveCounterAndSacrificeSelfOnLastEffect(CounterType.TIME));
        addEffect(EffectSlot.PRECOMBAT_MAIN_TRIGGERED, new DrawCardEffect(1));
    }
}
