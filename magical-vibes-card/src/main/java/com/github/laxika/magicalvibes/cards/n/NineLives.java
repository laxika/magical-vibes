package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ControllerLosesGameOnLeavesEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageToControllerAndPutCounterOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.StateTriggerEffect;

import java.util.List;

@CardRegistration(set = "M21", collectorNumber = "28")
public class NineLives extends Card {

    public NineLives() {
        addEffect(EffectSlot.STATIC,
                new PreventDamageToControllerAndPutCounterOnSelfEffect(CounterType.INCARNATION));
        addEffect(EffectSlot.STATE_TRIGGERED, new StateTriggerEffect(
                (gameData, sourcePermanent, controllerId) ->
                        sourcePermanent.getCounterCount(CounterType.INCARNATION) >= 9,
                List.of(new ExileSelfEffect()),
                "Nine Lives's state-triggered ability"));
        addEffect(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD, new ControllerLosesGameOnLeavesEffect());
    }
}
