package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CopyNextInstantOrSorceryCastThisTurnEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "40K", collectorNumber = "146")
public class TzaangorShaman extends Card {

    public TzaangorShaman() {
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                new CopyNextInstantOrSorceryCastThisTurnEffect());
    }
}
