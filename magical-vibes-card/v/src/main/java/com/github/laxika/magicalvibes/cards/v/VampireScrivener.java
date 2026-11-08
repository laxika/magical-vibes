package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.ControllerTurn;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SNC", collectorNumber = "98")
public class VampireScrivener extends Card {

    public VampireScrivener() {
        addTrigger(Triggers.youGainLife(),
                TriggerOptions.DEFAULT.withEventCondition(new ControllerTurn()),
                new PutCountersOnSourceEffect(1, 1, 1));
        addTrigger(Triggers.youLoseLife(),
                TriggerOptions.DEFAULT.withEventCondition(new ControllerTurn()),
                new PutCountersOnSourceEffect(1, 1, 1));
    }
}
