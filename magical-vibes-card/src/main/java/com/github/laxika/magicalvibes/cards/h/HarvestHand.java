package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.s.ScroungedScythe;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceTransformedFromGraveyardEffect;

@CardRegistration(set = "INR", collectorNumber = "265")
@CardRegistration(set = "SOI", collectorNumber = "256")
public class HarvestHand extends Card {

    public HarvestHand() {
        setBackFaceCard(new ScroungedScythe());

        // When this creature dies, return it to the battlefield transformed under your control.
        addEffect(EffectSlot.ON_DEATH, new ReturnSourceTransformedFromGraveyardEffect());
    }

    @Override
    public String getBackFaceClassName() {
        return "ScroungedScythe";
    }
}
