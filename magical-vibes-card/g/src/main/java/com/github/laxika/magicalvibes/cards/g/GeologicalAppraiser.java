package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DiscoverEffect;
import com.github.laxika.magicalvibes.model.condition.WasCast;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LCI", collectorNumber = "150")
@CardRegistration(set = "LCI", collectorNumber = "407")
public class GeologicalAppraiser extends Card {

    public GeologicalAppraiser() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new ConditionalEffect(new WasCast(), new DiscoverEffect(3)));
    }
}
