package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.o.OverloadedMageRing;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MOM", collectorNumber = "64")
public class InvasionOfVryn extends Card {

    public InvasionOfVryn() {
        setBackFaceCard(new OverloadedMageRing());
        addTrigger(Triggers.enters(ObjectRelation.THIS), new DrawCardEffect(3));
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new DiscardEffect(1, DiscardRecipient.CONTROLLER));
    }

    @Override
    public String getBackFaceClassName() {
        return "OverloadedMageRing";
    }
}
