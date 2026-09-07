package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterAndSacrificeSelfOnLastEffect;

@CardRegistration(set = "FUT", collectorNumber = "135")
public class RavagingRiftwurm extends Card {

    public RavagingRiftwurm() {
        addEffect(EffectSlot.STATIC, new KickerEffect("{4}"));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.TIME, new Fixed(2)));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ConditionalEffect(new Kicked(),
                new EnterWithCountersEffect(CounterType.TIME, new Fixed(3))));
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new RemoveCounterAndSacrificeSelfOnLastEffect(CounterType.TIME));
    }
}
