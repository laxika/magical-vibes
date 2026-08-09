package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterOrSacrificeSelfEffect;

@CardRegistration(set = "NEM", collectorNumber = "102")
public class Blastoderm extends Card {

    public Blastoderm() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.FADE, new Fixed(3)));
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new RemoveCounterOrSacrificeSelfEffect(CounterType.FADE));
    }
}
