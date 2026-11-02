package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "OPC2", collectorNumber = "5")
@CardRegistration(set = "OPCA", collectorNumber = "5")
@CardRegistration(set = "MOC", collectorNumber = "154")
public class PlanewideDisaster extends Card {

    public PlanewideDisaster() {
        addTrigger(Triggers.encounterThis(),
                new DestroyAllPermanentsEffect(new PermanentIsCreaturePredicate()));
    }
}
