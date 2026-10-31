package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.WaterbendCostPaid;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedCreatureDeathTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.WaterbendCost;

@CardRegistration(set = "TLA", collectorNumber = "118")
public class RuinousWaterbending extends Card {

    public RuinousWaterbending() {
        addCastingCost(WaterbendCost.optional(4));
        addSpellEffect(new BoostAllCreaturesEffect(-2, -2));
        addSpellEffect(new ConditionalEffect(new WaterbendCostPaid(),
                new RegisterDelayedCreatureDeathTriggerEffect(new GainLifeEffect(1))));
    }
}
