package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnCombatOpponentAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "5ED", collectorNumber = "166")
@CardRegistration(set = "HML", collectorNumber = "51")
public class GreaterWerewolf extends Card {

    public GreaterWerewolf() {
        // At end of combat, put a -0/-2 counter on each creature blocking or blocked by this creature.
        PutCounterOnCombatOpponentAtEndOfCombatEffect counter =
                new PutCounterOnCombatOpponentAtEndOfCombatEffect(CounterType.MINUS_ZERO_MINUS_TWO, 1);
        addTrigger(Triggers.endOfCombat(), counter);
    }
}
