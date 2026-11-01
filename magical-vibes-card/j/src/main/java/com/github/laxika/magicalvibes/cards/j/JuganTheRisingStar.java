package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.DistributeCountersAmongCreaturesOnDeathEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "CHK", collectorNumber = "217")
@CardRegistration(set = "MMA", collectorNumber = "150")
@CardRegistration(set = "IMA", collectorNumber = "172")
public class JuganTheRisingStar extends Card {

    public JuganTheRisingStar() {
        // When Jugan dies, you may distribute five +1/+1 counters among any number of target creatures.
        // The division is announced as the trigger goes on the stack; any creature on the battlefield
        // may receive counters, not just ones you control.
        addTrigger(Triggers.dies(ObjectRelation.THIS),
                DistributeCountersAmongCreaturesOnDeathEffect.fixedAmongAnyCreatures(
                        CounterType.PLUS_ONE_PLUS_ONE, 5));
    }
}
