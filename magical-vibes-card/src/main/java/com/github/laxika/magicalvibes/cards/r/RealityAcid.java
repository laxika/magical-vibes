package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterAndSacrificeSelfOnLastEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeEnchantedCreatureOnLeaveEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "PLC", collectorNumber = "45")
public class RealityAcid extends Card {

    public RealityAcid() {
        target(TargetFilters.permanent())
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new EnterWithCountersEffect(CounterType.TIME, new Fixed(3)))
                .addEffect(EffectSlot.UPKEEP_TRIGGERED,
                        new RemoveCounterAndSacrificeSelfOnLastEffect(CounterType.TIME))
                .addEffect(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD,
                        new SacrificeEnchantedCreatureOnLeaveEffect());
    }
}
