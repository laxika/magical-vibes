package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.c.CorvidSquall;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BecomePreparedEffect;

/** Galathul Galecaller // Corvid Squall (YSOS 3). */
@CardRegistration(set = "YSOS", collectorNumber = "3")
public class GalathulGalecallerCorvidSquall extends Card {

    public GalathulGalecallerCorvidSquall() {
        setBackFaceCard(new CorvidSquall());
        addEffect(EffectSlot.ON_ATTACK, new BecomePreparedEffect());
    }

    @Override
    public String getBackFaceClassName() {
        return "CorvidSquall";
    }
}
