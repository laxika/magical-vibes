package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "M21", collectorNumber = "25")
public class LightOfPromise extends Card {

    public LightOfPromise() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.STATIC, new GrantTriggeredAbilityEffect(
                        EffectSlot.ON_CONTROLLER_GAINS_LIFE,
                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE, new EventValue()),
                        GrantScope.ENCHANTED_CREATURE));
    }
}
