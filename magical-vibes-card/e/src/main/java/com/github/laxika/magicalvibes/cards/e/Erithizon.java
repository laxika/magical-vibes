package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MMQ", collectorNumber = "244")
public class Erithizon extends Card {

    public Erithizon() {
        target(TargetFilters.creature())
                .addTrigger(Triggers.attacks(),
                        new PutCountersEffect(CounterType.PLUS_ONE_PLUS_ONE));
        setCombatTriggerTargetChosenByDefendingPlayer(true);
    }
}
