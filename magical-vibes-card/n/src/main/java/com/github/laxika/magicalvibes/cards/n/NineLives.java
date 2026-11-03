package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.ControllerLosesGameOnLeavesEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageToControllerAndPutCounterOnSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M21", collectorNumber = "28")
@CardRegistration(set = "M21", collectorNumber = "344")
@CardRegistration(set = "SLD", collectorNumber = "793")
@CardRegistration(set = "MAR", collectorNumber = "3")
@CardRegistration(set = "OMB", collectorNumber = "3")
public class NineLives extends Card {

    public NineLives() {
        addStaticEffect(new PreventDamageToControllerAndPutCounterOnSelfEffect(CounterType.INCARNATION));
        addTrigger(Triggers.state((gameData, sourcePermanent, controllerId) ->
                        sourcePermanent.getCounterCount(CounterType.INCARNATION) >= 9),
                new ExileSelfEffect());
        addTrigger(Triggers.leavesBattlefield(ObjectRelation.THIS), new ControllerLosesGameOnLeavesEffect());
    }
}
