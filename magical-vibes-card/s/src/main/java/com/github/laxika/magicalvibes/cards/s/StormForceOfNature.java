package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GrantStormToNextInstantOrSorceryCastThisTurnEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SLD", collectorNumber = "1742")
@CardRegistration(set = "MAR", collectorNumber = "94")
public class StormForceOfNature extends Card {

    public StormForceOfNature() {
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                new GrantStormToNextInstantOrSorceryCastThisTurnEffect());
    }
}
