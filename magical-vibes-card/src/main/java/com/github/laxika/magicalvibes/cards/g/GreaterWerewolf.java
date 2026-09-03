package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.SourceAttackedOrBlockedThisCombat;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnCombatOpponentAtEndOfCombatEffect;

@CardRegistration(set = "5ED", collectorNumber = "166")
@CardRegistration(set = "HML", collectorNumber = "51")
public class GreaterWerewolf extends Card {

    public GreaterWerewolf() {
        // At end of combat, put a -0/-2 counter on each creature blocking or blocked by this creature.
        PutCounterOnCombatOpponentAtEndOfCombatEffect counter =
                new PutCounterOnCombatOpponentAtEndOfCombatEffect(CounterType.MINUS_ZERO_MINUS_TWO, 1);
        addEffect(EffectSlot.END_OF_COMBAT_TRIGGERED,
                new ConditionalEffect(new SourceAttackedOrBlockedThisCombat(), counter));
    }
}
