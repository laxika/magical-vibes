package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.u.UnhallowedCathar;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedReturnSourceTransformedEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DKA", collectorNumber = "13")
public class LoyalCatharUnhallowedCathar extends Card {

    public LoyalCatharUnhallowedCathar() {
        setBackFaceCard(new UnhallowedCathar());

        addTrigger(Triggers.dies(ObjectRelation.THIS), new RegisterDelayedReturnSourceTransformedEffect());
    }

    @Override
    public String getBackFaceClassName() {
        return "UnhallowedCathar";
    }
}
