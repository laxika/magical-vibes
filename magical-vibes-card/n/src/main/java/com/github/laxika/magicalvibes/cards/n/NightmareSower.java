package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ECL", collectorNumber = "114")
public class NightmareSower extends Card {

    public NightmareSower() {
        target(TargetFilters.creature(), 0, 1)
                .addTrigger(Triggers.youCastSpell().duringOpponentsTurn(),
                        new PutCounterOnTargetPermanentEffect(CounterType.MINUS_ONE_MINUS_ONE, 1));
    }
}
