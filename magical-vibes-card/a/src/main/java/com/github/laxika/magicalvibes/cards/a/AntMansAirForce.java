package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MSC", collectorNumber = "759")
public class AntMansAirForce extends Card {

    public AntMansAirForce() {
        target(TargetFilters.creature(), 0, 1)
                .addTrigger(Triggers.attacks(), new BoostTargetCreatureEffect(-1, 0));
    }
}
