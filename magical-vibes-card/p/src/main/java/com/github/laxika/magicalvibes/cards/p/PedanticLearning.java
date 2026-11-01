package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ODY", collectorNumber = "90")
public class PedanticLearning extends Card {

    public PedanticLearning() {
        addTrigger(Triggers.putIntoGraveyardFrom(Zone.LIBRARY, Triggers.LAND_CARD, ObjectRelation.YOU_OWN),
                new MayPayManaEffect("{1}", new DrawCardEffect(1), "Pay {1} to draw a card?"));
    }
}
