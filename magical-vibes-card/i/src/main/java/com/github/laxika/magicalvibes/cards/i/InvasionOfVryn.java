package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.o.OverloadedMageRing;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "MOM", collectorNumber = "64")
public class InvasionOfVryn extends Card {

    public InvasionOfVryn() {
        setBackFaceCard(new OverloadedMageRing());
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DrawCardEffect(3));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new DiscardEffect(1, DiscardRecipient.CONTROLLER));
    }

    @Override
    public String getBackFaceClassName() {
        return "OverloadedMageRing";
    }
}
