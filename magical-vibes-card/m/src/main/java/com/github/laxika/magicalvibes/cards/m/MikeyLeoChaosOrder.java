package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.YouPutCounterOnControlledCreatureTriggerEffect;

@CardRegistration(set = "TMT", collectorNumber = "158")
@CardRegistration(set = "TMT", collectorNumber = "200")
@CardRegistration(set = "TMT", collectorNumber = "247")
public class MikeyLeoChaosOrder extends Card {

    public MikeyLeoChaosOrder() {
        addEffect(EffectSlot.ON_ALLY_COUNTER_PUT_ON_CREATURE,
                new YouPutCounterOnControlledCreatureTriggerEffect(
                        new OncePerTurnTriggerEffect(new DrawCardEffect(1))));
    }
}
