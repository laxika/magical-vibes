package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.condition.SourcePowerAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FIC", collectorNumber = "64")
@CardRegistration(set = "FIC", collectorNumber = "157")
public class VincentVengefulAtoner extends Card {

    public VincentVengefulAtoner() {
        addTrigger(Triggers.creatureYouControlDealsCombatDamageToPlayer(),
                TriggerOptions.ONE_OR_MORE,
                PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE));
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                new ConditionalEffect(new SourcePowerAtLeast(7),
                        new DealDamageToPlayersEffect(new EventValue(), Subjects.except(Subjects.eachOpponent(), Subjects.triggeringPlayer()))));
    }
}
