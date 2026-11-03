package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.condition.Delirium;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SOI", collectorNumber = "220")
@CardRegistration(set = "SIR", collectorNumber = "207")
@CardRegistration(set = "DSC", collectorNumber = "191")
public class ObsessiveSkinner extends Card {

    public ObsessiveSkinner() {
        target(TargetFilters.creature())
                .addTrigger(Triggers.enters(ObjectRelation.THIS),
                        new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1))
                .addTrigger(Triggers.upkeep(ActivePlayer.OPPONENT),
                        TriggerOptions.DEFAULT.withInterveningIf(new Delirium()),
                        new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1));
    }
}
