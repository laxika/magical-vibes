package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BNG", collectorNumber = "28")
public class Sunbond extends Card {

    public Sunbond() {
        target(TargetFilters.creature())
                .addStaticEffect(new GrantTriggeredAbilityEffect(
                        new TriggeredAbility(Triggers.youGainLife(),
                                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE, new EventValue())),
                        GrantScope.ENCHANTED_CREATURE));
    }
}
