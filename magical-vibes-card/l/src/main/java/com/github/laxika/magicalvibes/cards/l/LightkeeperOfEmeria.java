package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.RepeatedAdditionalCostCount;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.RepeatableAdditionalManaCost;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "WWK", collectorNumber = "12")
@CardRegistration(set = "CMD", collectorNumber = "18")
@CardRegistration(set = "CMA", collectorNumber = "15")
public class LightkeeperOfEmeria extends Card {

    public LightkeeperOfEmeria() {
        addCastingCost(RepeatableAdditionalManaCost.multikicker(List.of("{W}")));
        addTrigger(Triggers.enters(ObjectRelation.THIS), new GainLifeEffect(
                new Scaled(new RepeatedAdditionalCostCount("{W}"), 2)));
    }
}
