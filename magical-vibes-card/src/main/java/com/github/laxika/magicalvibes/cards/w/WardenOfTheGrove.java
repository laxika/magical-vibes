package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.effect.EndureEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;

@CardRegistration(set = "TDM", collectorNumber = "166")
public class WardenOfTheGrove extends Card {

    public WardenOfTheGrove() {
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE));
        addEffect(EffectSlot.ON_ALLY_NONTOKEN_CREATURE_ENTERS_BATTLEFIELD,
                EndureEffect.forTriggeringPermanent(
                        new CountersOnSource(CounterType.PLUS_ONE_PLUS_ONE)));
    }
}
