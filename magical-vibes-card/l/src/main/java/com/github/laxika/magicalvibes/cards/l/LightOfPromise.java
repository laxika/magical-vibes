package com.github.laxika.magicalvibes.cards.l;

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

@CardRegistration(set = "M21", collectorNumber = "25")
@CardRegistration(set = "MAR", collectorNumber = "46")
public class LightOfPromise extends Card {

    public LightOfPromise() {
        target(TargetFilters.creature())
                .addStaticEffect(new GrantTriggeredAbilityEffect(
                        new TriggeredAbility(Triggers.youGainLife(),
                                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE, new EventValue())),
                        GrantScope.ENCHANTED_CREATURE));
    }
}
