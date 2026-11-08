package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.amount.TriggeringPermanentPower;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "OTJ", collectorNumber = "175")
public class RailwayBrawler extends Card {

    public RailwayBrawler() {
        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL),
                PutCounterOnTargetPermanentEffect.onTriggeringPermanent(CounterType.PLUS_ONE_PLUS_ONE, new TriggeringPermanentPower()));

        // Plot {3}{G}
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{3}{G}"))));
    }
}
