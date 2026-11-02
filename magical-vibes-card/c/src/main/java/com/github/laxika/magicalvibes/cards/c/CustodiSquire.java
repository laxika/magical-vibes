package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.WillOfTheCouncilEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "CMM", collectorNumber = "19")
@CardRegistration(set = "VOC", collectorNumber = "83")
public class CustodiSquire extends Card {

    public CustodiSquire() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new WillOfTheCouncilEffect(true));
    }
}
