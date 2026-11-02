package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.r.Replenish;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.MinimumAttackers;
import com.github.laxika.magicalvibes.model.effect.BecomePreparedEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

/** Eiganjo Dynastorian // Replenish (SOC 13). */
@CardRegistration(set = "SOC", collectorNumber = "13")
@CardRegistration(set = "SOC", collectorNumber = "63")
public class EiganjoDynastorianReplenish extends Card {

    public EiganjoDynastorianReplenish() {
        setBackFaceCard(new Replenish());

        addTrigger(Triggers.youAttack(),
                new ConditionalEffect(new MinimumAttackers(2), new BecomePreparedEffect()));
    }

    @Override
    public String getBackFaceClassName() {
        return "Replenish";
    }
}
