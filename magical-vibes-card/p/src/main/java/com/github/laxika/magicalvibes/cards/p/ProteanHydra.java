package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageAndRemovePlusOnePlusOneCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedStepTriggerEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M10", collectorNumber = "200")
@CardRegistration(set = "M11", collectorNumber = "194")
public class ProteanHydra extends Card {

    public ProteanHydra() {
        addStaticEffect(new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new XValue()));
        addStaticEffect(new PreventDamageAndRemovePlusOnePlusOneCountersEffect());
        addTrigger(Triggers.plusOneCounterRemovedFromThis(), RegisterDelayedStepTriggerEffect.aboutSourceAtNext(
                Triggers.endStep(ActivePlayer.ANY), new PutCountersOnSourceEffect(1, 1, 2)));
    }
}
