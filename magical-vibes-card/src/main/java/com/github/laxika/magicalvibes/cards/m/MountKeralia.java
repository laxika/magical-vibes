package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageFromNamedPlanesToControlledPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;

@CardRegistration(set = "OPC2", collectorNumber = "25")
public class MountKeralia extends Card {

    public MountKeralia() {
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                new PutCountersOnSelfEffect(CounterType.PRESSURE));
        addEffect(EffectSlot.PLANESWALK_FROM_TRIGGERED,
                new MassDamageEffect(new CountersOnSource(CounterType.PRESSURE), false, true, null));
        addEffect(EffectSlot.CHAOS_TRIGGERED,
                new PreventDamageFromNamedPlanesToControlledPermanentsEffect("Mount Keralia"));
    }
}
