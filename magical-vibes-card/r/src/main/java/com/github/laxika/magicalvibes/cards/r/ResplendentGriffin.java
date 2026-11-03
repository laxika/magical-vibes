package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.ControllerHasCityBlessing;
import com.github.laxika.magicalvibes.model.effect.AscendEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "RIX", collectorNumber = "170")
public class ResplendentGriffin extends Card {

    public ResplendentGriffin() {
        addStaticEffect(new AscendEffect());
        addTrigger(Triggers.attacks(),
                TriggerOptions.DEFAULT.withInterveningIf(new ControllerHasCityBlessing()),
                new PutCountersOnSourceEffect(1, 1, 1));
    }
}
