package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.ControllerTurn;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BLB", collectorNumber = "39")
public class WaxWaneWitness extends Card {

    public WaxWaneWitness() {
        addTrigger(Triggers.youGainLife(), new ConditionalEffect(
                new ControllerTurn(), new BoostSelfEffect(1, 0)));
        addTrigger(Triggers.youLoseLife(), new ConditionalEffect(
                new ControllerTurn(), new BoostSelfEffect(1, 0)));
    }
}
