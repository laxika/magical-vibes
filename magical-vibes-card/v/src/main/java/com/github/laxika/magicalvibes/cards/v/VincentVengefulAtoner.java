package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.condition.SourcePowerAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FIC", collectorNumber = "64")
@CardRegistration(set = "FIC", collectorNumber = "157")
public class VincentVengefulAtoner extends Card {

    public VincentVengefulAtoner() {
        addTrigger(Triggers.creatureYouControlDealsCombatDamageToPlayer(),
                TriggerOptions.ONE_OR_MORE,
                new PutCountersOnSourceEffect(1, 1, 1));
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                new ConditionalEffect(new SourcePowerAtLeast(7),
                        new DealDamageToPlayersEffect(new EventValue(), DamageRecipient.EACH_OTHER_OPPONENT)));
    }
}
