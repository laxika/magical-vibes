package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "PIP", collectorNumber = "88")
@CardRegistration(set = "PIP", collectorNumber = "616")
public class WellRested extends Card {

    public WellRested() {
        target(TargetFilters.creature()).addStaticEffect(new GrantTriggeredAbilityEffect(
                EffectSlot.ON_SELF_BECOMES_UNTAPPED,
                OncePerTurnTriggerEffect.perGrant(SequenceEffect.of(
                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE, 2),
                        new GainLifeEffect(2),
                        new DrawCardEffect(1))),
                GrantScope.ENCHANTED_CREATURE));
    }
}
