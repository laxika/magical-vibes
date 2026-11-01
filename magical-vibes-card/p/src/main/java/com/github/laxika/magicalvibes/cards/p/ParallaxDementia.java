package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.DestroyEnchantedCreatureOnLeaveEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterOrSacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "NEM", collectorNumber = "62")
public class ParallaxDementia extends Card {

    public ParallaxDementia() {
        target(TargetFilters.creature())
                .addStaticEffect(new EnterWithCountersEffect(CounterType.FADE, new Fixed(1)))
                .addEffect(EffectSlot.UPKEEP_TRIGGERED,
                        new RemoveCounterOrSacrificeSelfEffect(CounterType.FADE))
                .addStaticEffect(new StaticBoostEffect(3, 2, GrantScope.ENCHANTED_CREATURE))
                .addTrigger(Triggers.leavesBattlefield(ObjectRelation.THIS),
                        new DestroyEnchantedCreatureOnLeaveEffect());
    }
}
