package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.b.Boltwave;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BecomePreparedEffect;

/** Crescendo Conductor // Boltwave (YSOS 9). */
@CardRegistration(set = "YSOS", collectorNumber = "9")
public class CrescendoConductorBoltwave extends Card {

    public CrescendoConductorBoltwave() {
        setBackFaceCard(new Boltwave());

        addEffect(EffectSlot.ON_CONTROLLER_CONJURES, new BecomePreparedEffect());
    }

    @Override
    public String getBackFaceClassName() {
        return "Boltwave";
    }
}
