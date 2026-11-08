package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.ControllerTurn;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "BLB", collectorNumber = "39")
public class WaxWaneWitness extends Card {

    public WaxWaneWitness() {
        addTrigger(List.of(Triggers.youGainLife(), Triggers.youLoseLife()),
                TriggerOptions.DEFAULT.withEventCondition(new ControllerTurn()),
                new BoostSelfEffect(1, 0));
    }
}
