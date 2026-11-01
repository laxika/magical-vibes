package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DIS", collectorNumber = "104")
public class AzoriusAethermage extends Card {

    public AzoriusAethermage() {
        addTrigger(Triggers.putIntoHandFrom(Zone.BATTLEFIELD, null, ObjectRelation.YOU_OWN),
                new MayPayManaEffect("{1}", new DrawCardEffect(), "Pay {1} to draw a card?"));
    }
}
