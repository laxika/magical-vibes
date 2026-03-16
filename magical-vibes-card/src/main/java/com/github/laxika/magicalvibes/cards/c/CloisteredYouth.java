package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.u.UnholyFiend;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;

@CardRegistration(set = "ISD", collectorNumber = "8")
public class CloisteredYouth extends Card {

    public CloisteredYouth() {
        // Set up back face
        UnholyFiend backFace = new UnholyFiend();
        backFace.setSetCode(getSetCode());
        backFace.setCollectorNumber(getCollectorNumber());
        setBackFaceCard(backFace);

        // At the beginning of your upkeep, you may transform Cloistered Youth.
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new MayEffect(
                new TransformSelfEffect(),
                "Transform Cloistered Youth?"
        ));
    }

    @Override
    public String getBackFaceClassName() {
        return "UnholyFiend";
    }
}
