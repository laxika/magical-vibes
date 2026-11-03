package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.condition.OpponentLostLifeLastTurn;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M15", collectorNumber = "96")
public class FeastOnTheFallen extends Card {

    public FeastOnTheFallen() {
        target(TargetFilters.creatureYouControl())
                .addTrigger(Triggers.upkeep(ActivePlayer.ANY),
                        TriggerOptions.DEFAULT.withInterveningIf(new OpponentLostLifeLastTurn()),
                        new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1));
    }
}
