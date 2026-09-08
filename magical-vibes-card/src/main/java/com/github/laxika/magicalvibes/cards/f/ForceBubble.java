package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DamageToControllerCounterReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveAllCountersEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.StateTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAtLeastCountersPredicate;

import java.util.List;

@CardRegistration(set = "SCG", collectorNumber = "14")
public class ForceBubble extends Card {

    public ForceBubble() {
        addEffect(EffectSlot.STATIC,
                new DamageToControllerCounterReplacementEffect(CounterType.DEPLETION));
        addEffect(EffectSlot.STATE_TRIGGERED, new StateTriggerEffect(
                new PermanentHasAtLeastCountersPredicate(CounterType.DEPLETION, 4),
                List.of(new SacrificeSelfEffect()),
                "Force Bubble's state-triggered ability"));
        addEffect(EffectSlot.END_STEP_TRIGGERED,
                new RemoveAllCountersEffect(CounterType.DEPLETION));
    }
}
