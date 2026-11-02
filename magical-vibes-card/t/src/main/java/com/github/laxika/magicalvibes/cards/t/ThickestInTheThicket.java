package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.TargetPower;
import com.github.laxika.magicalvibes.model.condition.ControlsCreatureWithGreatestPower;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BLC", collectorNumber = "34")
@CardRegistration(set = "BLC", collectorNumber = "67")
@CardRegistration(set = "FDC", collectorNumber = "236")
public class ThickestInTheThicket extends Card {

    public ThickestInTheThicket() {
        target(TargetFilters.creature()).addTrigger(Triggers.enters(ObjectRelation.THIS),
                new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, new TargetPower()));

        addTrigger(Triggers.endStep(ActivePlayer.YOU), new ConditionalEffect(
                new ControlsCreatureWithGreatestPower(), new DrawCardEffect(2), false));
    }
}
