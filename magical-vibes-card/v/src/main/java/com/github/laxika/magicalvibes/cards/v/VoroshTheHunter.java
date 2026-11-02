package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "PLC", collectorNumber = "164")
@CardRegistration(set = "CMD", collectorNumber = "235")
@CardRegistration(set = "CM2", collectorNumber = "3")
public class VoroshTheHunter extends Card {

    public VoroshTheHunter() {
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                new MayPayManaEffect("{2}{G}",
                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE, 6),
                        "Pay {2}{G}?"));
    }
}
