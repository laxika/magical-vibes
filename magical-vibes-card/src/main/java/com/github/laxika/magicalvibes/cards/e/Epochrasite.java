package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.CastNotFromHand;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSourceCardWithSuspendCountersEffect;

@CardRegistration(set = "FUT", collectorNumber = "162")
public class Epochrasite extends Card {

    public Epochrasite() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ConditionalEffect(new CastNotFromHand(),
                new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new Fixed(3))));
        addEffect(EffectSlot.ON_DEATH, new ExileSourceCardWithSuspendCountersEffect(3));
    }
}
