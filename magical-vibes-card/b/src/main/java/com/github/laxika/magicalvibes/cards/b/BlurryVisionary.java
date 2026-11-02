package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateModalDoubleFacedCardFromTopTwoEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MB2", collectorNumber = "290")
@CardRegistration(set = "MB2", collectorNumber = "526")
public class BlurryVisionary extends Card {

    public BlurryVisionary() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new CreateModalDoubleFacedCardFromTopTwoEffect());
    }
}
