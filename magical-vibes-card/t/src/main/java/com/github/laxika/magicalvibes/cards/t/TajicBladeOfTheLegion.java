package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.MinimumAttackers;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DGM", collectorNumber = "107")
@CardRegistration(set = "DMC", collectorNumber = "171")
public class TajicBladeOfTheLegion extends Card {

    public TajicBladeOfTheLegion() {
        addTrigger(Triggers.attacks(), new ConditionalEffect(
                new MinimumAttackers(3),
                new BoostSelfEffect(5, 5)));
    }
}
