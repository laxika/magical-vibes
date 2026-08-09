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

@CardRegistration(set = "NEM", collectorNumber = "62")
public class ParallaxDementia extends Card {

    public ParallaxDementia() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new EnterWithCountersEffect(CounterType.FADE, new Fixed(1)))
                .addEffect(EffectSlot.UPKEEP_TRIGGERED,
                        new RemoveCounterOrSacrificeSelfEffect(CounterType.FADE))
                .addEffect(EffectSlot.STATIC, new StaticBoostEffect(3, 2, GrantScope.ENCHANTED_CREATURE))
                .addEffect(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD,
                        new DestroyEnchantedCreatureOnLeaveEffect());
    }
}
