package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.u.UnhallowedCathar;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedReturnSourceTransformedEffect;

@CardRegistration(set = "DKA", collectorNumber = "13")
public class LoyalCatharUnhallowedCathar extends Card {

    public LoyalCatharUnhallowedCathar() {
        UnhallowedCathar backFace = new UnhallowedCathar();
        backFace.setSetCode(getSetCode());
        backFace.setCollectorNumber(getCollectorNumber());
        setBackFaceCard(backFace);

        addEffect(EffectSlot.ON_DEATH, new RegisterDelayedReturnSourceTransformedEffect());
    }

    @Override
    public String getBackFaceClassName() {
        return "UnhallowedCathar";
    }
}
