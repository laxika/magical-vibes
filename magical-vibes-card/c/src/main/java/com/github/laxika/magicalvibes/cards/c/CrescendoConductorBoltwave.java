package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.b.Boltwave;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BecomePreparedEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

/** Crescendo Conductor // Boltwave (YSOS 9). */
@CardRegistration(set = "YSOS", collectorNumber = "9")
public class CrescendoConductorBoltwave extends Card {

    public CrescendoConductorBoltwave() {
        setBackFaceCard(new Boltwave());

        addTrigger(Triggers.youConjure(), new BecomePreparedEffect());
    }

    @Override
    public String getBackFaceClassName() {
        return "Boltwave";
    }
}
