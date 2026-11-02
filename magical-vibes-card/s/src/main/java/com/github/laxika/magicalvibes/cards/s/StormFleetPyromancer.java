package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.condition.Raid;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "XLN", collectorNumber = "163")
@CardRegistration(set = "2X2", collectorNumber = "126")
public class StormFleetPyromancer extends Card {

    public StormFleetPyromancer() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new ConditionalEffect(new Raid(), new DealDamageToAnyTargetEffect(2)));
    }
}
